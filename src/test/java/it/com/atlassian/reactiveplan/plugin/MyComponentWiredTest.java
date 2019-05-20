package it.com.atlassian.reactiveplan.plugin;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.reactiveplan.plugin.api.MyPluginComponent;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

 @RunWith(AtlassianPluginsTestRunner.class)
public class MyComponentWiredTest
{

    private final ApplicationProperties applicationProperties;
    private final MyPluginComponent myPluginComponent;

    public MyComponentWiredTest(ApplicationProperties applicationProperties,MyPluginComponent myPluginComponent)
    {
        this.applicationProperties = applicationProperties;
        this.myPluginComponent = myPluginComponent;
    }
    @Test
    public void testMyName()
    {
        assertTrue(true);
    }
}