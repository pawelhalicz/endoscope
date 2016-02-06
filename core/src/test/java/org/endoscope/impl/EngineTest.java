package org.endoscope.impl;

import org.junit.Assert;
import org.junit.Test;

public class EngineTest {
    @Test
    public void test_flow(){
        Engine ci = new Engine();
        ci.setEnabled(true);

        ci.push("a1");
        ci.push("a11");
        ci.pop();
        ci.pop();

        ci.process( map -> {
            Assert.assertEquals(2, map.size());
            Assert.assertTrue(map.containsKey("a1"));
            Assert.assertTrue(map.containsKey("a11"));
            Assert.assertEquals(1, map.get("a1").getChildren().size());
            Assert.assertNotNull(map.get("a1").getChildren().get("a11"));
        });
    }
}