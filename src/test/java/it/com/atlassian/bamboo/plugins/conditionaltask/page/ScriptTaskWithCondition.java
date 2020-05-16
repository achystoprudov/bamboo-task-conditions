package it.com.atlassian.bamboo.plugins.conditionaltask.page;

import com.atlassian.bamboo.pageobjects.pages.tasks.ScriptTaskComponent;
import com.atlassian.pageobjects.elements.PageElementFinder;

import javax.inject.Inject;
import java.util.Map;

public class ScriptTaskWithCondition extends ScriptTaskComponent {

    @Inject
    private PageElementFinder elementFinder;

    @Override
    public void updateTaskDetails(final Map<String, String> config) {
        super.updateTaskDetails(config);
        ConditionalTaskHelper.applyConditionalSettings(elementFinder, config);
    }
}
