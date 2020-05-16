package it.com.atlassian.bamboo.plugins.conditionaltask;

import com.atlassian.bamboo.pageobjects.pages.plan.configuration.JobTaskConfigurationPage;
import com.atlassian.bamboo.plan.PlanKey;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.specs.api.builders.plan.Job;
import com.atlassian.bamboo.specs.api.builders.plan.Plan;
import com.atlassian.bamboo.specs.api.builders.plan.Stage;
import com.atlassian.bamboo.specs.api.builders.project.Project;
import com.atlassian.bamboo.specs.api.builders.repository.VcsChangeDetection;
import com.atlassian.bamboo.specs.builders.repository.bitbucket.cloud.BitbucketCloudRepository;
import com.atlassian.bamboo.specs.builders.repository.viewer.BitbucketCloudRepositoryViewer;
import com.atlassian.bamboo.specs.builders.task.ScriptTask;
import com.atlassian.bamboo.testutils.AcceptanceTestHelper;
import com.atlassian.bamboo.testutils.junit.rule.RemoteAgentRule;
import com.atlassian.bamboo.webdriver.BambooWebDriverTest;
import it.com.atlassian.bamboo.plugins.conditionaltask.page.ScriptTaskWithCondition;
import org.junit.Before;
import org.junit.ClassRule;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;

public class ConditionsTest extends BambooWebDriverTest {
    @ClassRule
    public static final RemoteAgentRule remoteAgentRule = new RemoteAgentRule();

    private Properties textProperties;

    @Before
    public void setUp() {
        textProperties = AcceptanceTestHelper.loadProperties("com/atlassian/bamboo/plugins/task_conditions/i18n.properties");
    }

    @Override
    protected void onAfter() {

    }

    protected void tryToAddConditionalTask(Map<String, String> config,
                                           PlanKey fullPlanKey,
                                           String expectedErrorMessage) throws Exception {
        final JobTaskConfigurationPage taskConfigurationPage = product.visit(JobTaskConfigurationPage.class,
                PlanKeys.getJobKey(fullPlanKey, "JOB1"));
        final List<String> errors = taskConfigurationPage.addNewTaskAndExpectFailure("Script", ScriptTaskWithCondition.class, "", config);
        assertThat(errors.size(), greaterThanOrEqualTo(1));
        assertThat(errors.get(0), equalTo(expectedErrorMessage));
    }

    protected Plan getPlan(String projectKey, String planKey, String oldDescription) {
        return new Plan(new Project().name(projectKey).key(projectKey), planKey, planKey)
                .stages(
                        new Stage("Default").jobs(
                                new Job("Default", "JOB1").tasks(
                                        new ScriptTask()
                                                .description(oldDescription)
                                                .inlineBody("echo hi")
                                )
                        )
                );
    }

    protected Plan getPlanWithRepository(String projectKey, String planKey, String oldDescription) {
        return getPlan(projectKey, planKey, oldDescription)
                .planRepositories(new BitbucketCloudRepository()
                        .name("Test")
                        .repositoryViewer(new BitbucketCloudRepositoryViewer())
                        .repositorySlug("atlassian", "bamboo-docker-plugin")
                        .branch("master")
                        .changeDetection(new VcsChangeDetection()));
    }


    protected String getTextLocal(String key, Object... arguments) {
        return MessageFormat.format(textProperties.getProperty(key), arguments);
    }
}
