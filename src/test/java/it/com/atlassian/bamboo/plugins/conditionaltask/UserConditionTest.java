package it.com.atlassian.bamboo.plugins.conditionaltask;

import com.atlassian.bamboo.pageobjects.pages.plan.PlanSummaryPage;
import com.atlassian.bamboo.pageobjects.pages.plan.configuration.JobTaskConfigurationPage;
import com.atlassian.bamboo.plan.PlanKey;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.plugins.task_conditions.user.UserCondition;
import com.atlassian.bamboo.specs.api.builders.permission.PermissionType;
import com.atlassian.bamboo.specs.api.builders.permission.Permissions;
import com.atlassian.bamboo.testutils.UniqueNameHelper;
import com.atlassian.bamboo.testutils.specs.TestPlanSpecsHelper;
import com.atlassian.bamboo.testutils.user.TestUser;
import com.google.common.collect.ImmutableMap;
import it.com.atlassian.bamboo.plugins.conditionaltask.page.ScriptTaskWithCondition;
import org.junit.Test;

import java.util.Map;

import static it.com.atlassian.bamboo.plugins.conditionaltask.page.ConditionalTaskHelper.CONDITION_PLUGIN_KEY;

public class UserConditionTest extends ConditionsTest {

    /**
     * Check that task with user condition is executed when started by user1
     * and skipped when executed by user2.
     */
    @Test
    public void metCondition() throws Exception {
        //create 2 users
        final TestUser user1 = backdoor.users().createTemporaryUser();
        final TestUser user2 = backdoor.users().createTemporaryUser();
        //create task with user condition
        final String projectKey = UniqueNameHelper.makeUniqueName("USERCOND");
        final String planKey = UniqueNameHelper.makeUniqueName("COND");
        String oldDescription = "Task description";
        final PlanKey fullPlanKey = TestPlanSpecsHelper.getPlanKey(backdoor.plans().createPlan(getPlan(projectKey, planKey, oldDescription)));
        final Permissions permissions = new Permissions()
                .loggedInUserPermissions(PermissionType.VIEW, PermissionType.BUILD);
        backdoor.permissions().setPlanPermissions(fullPlanKey, permissions);
        product.fastLogin(TestUser.ADMIN);
        final JobTaskConfigurationPage taskConfigurationPage = product.visit(JobTaskConfigurationPage.class, PlanKeys.getJobKey(fullPlanKey, "JOB1"));
        final Map<String, String> config = ImmutableMap.of("conditional", "true",
                CONDITION_PLUGIN_KEY, UserCondition.PLUGIN_KEY,
                UserCondition.USERNAME_PARAM, user1.getUsername());
        taskConfigurationPage.editTask(oldDescription, ScriptTaskWithCondition.class, oldDescription, config);
        //confirm it's green when executed by one user
        startBuildAndWaitForResult(user1, fullPlanKey, 1, true);
        //confirm it's red when executed by another user
        startBuildAndWaitForResult(user2, fullPlanKey, 2, false);
    }

    @Test
    public void checkUsernameIsValidated() throws Exception {
        final String projectKey = UniqueNameHelper.makeUniqueName("USERCOND");
        final String planKey = UniqueNameHelper.makeUniqueName("COND");
        final String description = "Some description";
        final PlanKey fullPlanKey = TestPlanSpecsHelper.getPlanKey(backdoor.plans().createPlan(getPlan(projectKey, planKey, description)));
        product.fastLogin(TestUser.ADMIN);

        tryToAddConditionalTask(ImmutableMap.of("conditional", "true",
                CONDITION_PLUGIN_KEY, UserCondition.PLUGIN_KEY,
                UserCondition.USERNAME_PARAM, "",
                "body", "ls -l"), fullPlanKey, getTextLocal("user.condition.error.user.value.required")
        );

        final String userName = "vasiliy";
        tryToAddConditionalTask(ImmutableMap.of("conditional", "true",
                CONDITION_PLUGIN_KEY, UserCondition.PLUGIN_KEY,
                UserCondition.USERNAME_PARAM, userName,
                "body", "ls -l"), fullPlanKey, getTextLocal("user.condition.error.user.not.found", userName)
        );


    }

    private void startBuildAndWaitForResult(TestUser user, PlanKey planKey, int buildNumber, boolean success) {
        product.logoutByUrl();
        product.fastLogin(user);
        final PlanSummaryPage summaryPage = product.visit(PlanSummaryPage.class, planKey);
        summaryPage.startPlan();
        if (success) {
            backdoor.plans().waitForSuccessfulBuild(planKey, buildNumber);
        } else {
            backdoor.plans().waitForFailedBuild(planKey, buildNumber);
        }
    }
}
