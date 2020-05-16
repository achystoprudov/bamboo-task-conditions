package com.atlassian.bamboo.plugins.task_conditions.user;

import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinition;
import com.atlassian.bamboo.v2.build.CommonContext;
import com.atlassian.bamboo.v2.build.trigger.ManualBuildTriggerReason;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserConditionTest {
    private UserCondition condition;

    @Before
    public void setUp() {
        condition = new UserCondition();
    }

    @Test
    public void meetCondition() {
        //given
        final String user = "user";
        final Map<String, String> conditionConfiguration = ImmutableMap.of(UserCondition.USERNAME_CONF, user);
        final CommonContext commonContext = mock(CommonContext.class);
        initTriggerReason(user, commonContext);
        //when
        final boolean meet = condition.isMet(mock(RuntimeTaskDefinition.class), conditionConfiguration, commonContext);
        //then
        assertThat("user triggered build but condition didn't meet", meet, is(true));
    }

    @Test
    public void dontMeetCondition() {
        //given
        final Map<String, String> conditionConfiguration = ImmutableMap.of(UserCondition.USERNAME_CONF, "admin");
        final CommonContext commonContext = mock(CommonContext.class);
        initTriggerReason("user", commonContext);
        //when
        final boolean meet = condition.isMet(mock(RuntimeTaskDefinition.class), conditionConfiguration, commonContext);
        //then
        assertThat("'user' is not 'admin'", meet, is(false));
    }

    private void initTriggerReason(String user, CommonContext commonContext) {
        final ManualBuildTriggerReason triggerReason = new ManualBuildTriggerReason();
        triggerReason.setUserName(user);
        when(commonContext.getTriggerReason()).thenReturn(triggerReason);
    }
}