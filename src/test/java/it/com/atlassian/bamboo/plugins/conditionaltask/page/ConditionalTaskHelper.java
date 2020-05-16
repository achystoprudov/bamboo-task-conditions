package it.com.atlassian.bamboo.plugins.conditionaltask.page;

import com.atlassian.bamboo.plugins.task_conditions.user.UserCondition;
import com.atlassian.pageobjects.elements.CheckboxElement;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.SelectElement;
import com.atlassian.pageobjects.elements.query.Poller;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;

import java.util.Map;

public class ConditionalTaskHelper {

    public static final String CONDITION_PLUGIN_KEY = "conditionPluginKey";
    static void applyConditionalSettings(PageElementFinder elementFinder, Map<String, String> config) {
        if (config.containsKey("conditional")) {
            CheckboxElement conditionalCheckbox = elementFinder.find(By.name("conditionalTask"), CheckboxElement.class);
            conditionalCheckbox.check();
            SelectElement conditionSelect = elementFinder.find(By.name("selectedCondition"), SelectElement.class);
            conditionSelect.select(Options.value(config.get(CONDITION_PLUGIN_KEY)));
            if (config.containsValue(UserCondition.PLUGIN_KEY)) {
                final PageElement userNameCondition = elementFinder.find(By.cssSelector("div.user-picker input.text"));
                Poller.waitUntilTrue(userNameCondition.timed().isVisible());
                userNameCondition.type(config.get(UserCondition.USERNAME_PARAM));
            }
        }
    }

    private static void setValueField(Map<String, String> config, String valueKey, PageElementFinder elementFinder) {
        if (config.get(valueKey) != null) {
            final PageElement variableValue = elementFinder.find(By.name(valueKey));
            Poller.waitUntilTrue(variableValue.timed().isVisible());
            variableValue.type(StringUtils.defaultString(config.get(valueKey)));
        }
    }
}
