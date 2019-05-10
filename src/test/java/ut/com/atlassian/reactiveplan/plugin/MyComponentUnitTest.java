package ut.com.atlassian.reactiveplan.plugin;

import org.junit.Test;
import com.atlassian.reactiveplan.plugin.api.MyPluginComponent;
import com.atlassian.reactiveplan.plugin.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}