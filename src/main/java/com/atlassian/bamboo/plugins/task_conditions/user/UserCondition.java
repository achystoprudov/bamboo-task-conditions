package com.atlassian.bamboo.plugins.task_conditions.user;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.condition.TaskCondition;
import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinition;
import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.error.SimpleErrorCollection;
import com.atlassian.bamboo.v2.build.CommonContext;
import com.atlassian.bamboo.v2.build.trigger.ManualBuildTriggerReason;
import com.atlassian.bamboo.v2.build.trigger.TriggerReason;
import com.atlassian.plugin.spring.scanner.annotation.imports.BambooImport;
import com.atlassian.sal.api.message.I18nResolver;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserCondition implements TaskCondition {

    static final String USERNAME_CONF = "username";
    public static final String USERNAME_PARAM = "task.condition.username";
    public static final String PLUGIN_KEY = "com.atlassian.bamboo.plugins.bamboo-conditional-tasks:userCondition";

    @Autowired(required = false)
    @BambooImport
    private I18nResolver textProvider;
    @Autowired(required = false)
    @BambooImport
    private BambooUserManager bambooUserManager;

    @Override
    public boolean isMet(@NotNull RuntimeTaskDefinition taskDefinition,
                         @NotNull Map<String, String> conditionConfiguration,
                         @NotNull CommonContext commonContext) {
        final String usernameCondition = conditionConfiguration.get(USERNAME_CONF);
        final TriggerReason triggerReason = commonContext.getTriggerReason();
        if (triggerReason instanceof ManualBuildTriggerReason) {
            final String userStartedJob = ((ManualBuildTriggerReason) triggerReason).getUserName();
            return Objects.equals(userStartedJob, usernameCondition);
        }
        return false;
    }

    @NotNull
    @Override
    public Map<String, String> prepareConfiguration(@NotNull ActionParametersMap parametersMap) {
        Map<String, String> result = new HashMap<>();
        result.put(USERNAME_CONF, parametersMap.getString(USERNAME_PARAM, ""));
        return result;
    }

    @NotNull
    @Override
    public Map<String, Object> prepareParametersMap(@NotNull Map<String, String> configuration, ActionParametersMap parametersMap) {
        final Map<String, Object> result = new HashMap<>();
        if(StringUtils.isBlank(parametersMap.getString(USERNAME_PARAM))) {
            result.put(USERNAME_PARAM, configuration.get(USERNAME_CONF));
        } else {
            result.put(USERNAME_PARAM, parametersMap.getString(USERNAME_PARAM));
        }
        return result;
    }

    @Nullable
    @Override
    public ErrorCollection validate(@NotNull ActionParametersMap parametersMap) {
        ErrorCollection errorCollection = new SimpleErrorCollection();
        final String userName = parametersMap.getString(USERNAME_PARAM);
        if (StringUtils.isBlank(userName)) {
            errorCollection.addError(USERNAME_PARAM, textProvider.getText("user.condition.error.user.value.required"));
        } else {
            if (bambooUserManager.getUser(userName) == null) {
                errorCollection.addError(USERNAME_PARAM, textProvider.getText("user.condition.error.user.not.found", userName));

            }
        }
        return errorCollection;
    }
}
