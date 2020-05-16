package com.atlassian.bamboo.plugins.task_conditions.user;

import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.plan.TopLevelPlan;
import com.atlassian.bamboo.plan.cache.ImmutableJob;
import com.atlassian.bamboo.task.TaskConditionConfig;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.user.rename.UserRenameEvent;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.spring.scanner.annotation.component.BambooComponent;
import com.atlassian.plugin.spring.scanner.annotation.imports.BambooImport;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.Objects;

@BambooComponent
public class UserRenameListener {
    private final EventPublisher eventPublisher;
    private final PlanManager planManager;

    @Inject
    public UserRenameListener(@BambooImport("eventPublisher") final EventPublisher eventPublisher,
                              @BambooImport final PlanManager planManager) {
        this.eventPublisher = eventPublisher;
        this.planManager = planManager;
    }

    @PostConstruct
    private void postConstruct() {
        eventPublisher.register(this);
    }

    @PreDestroy
    private void preDestroy() {
        eventPublisher.unregister(this);
    }

    @EventListener
    public void onUserRename(UserRenameEvent event) {
        for (TopLevelPlan plan : planManager.getAllPlans(TopLevelPlan.class)) {
            boolean shouldBeSaved = false;
            for (ImmutableJob job : plan.getAllJobs()) {
                for (TaskDefinition taskDefinition : job.getTaskDefinitions()) {
                    for (TaskConditionConfig condition : taskDefinition.getConditions()) {
                        if (Objects.equals(condition.getConditionPluginKey(), UserCondition.PLUGIN_KEY)
                                && Objects.equals(condition.getConfiguration().get(UserCondition.USERNAME_CONF), event.getOldUserName())) {
                            condition.getConfiguration().put(UserCondition.USERNAME_CONF, event.getNewUserName());
                            shouldBeSaved = true;
                        }
                    }
                }
            }
            if (shouldBeSaved) {
                planManager.savePlan(plan);
            }
        }
    }
}
