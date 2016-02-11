package org.endoscope.impl;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class ContextEngineTest {
    @Test
    public void test_flow(){
        ContextEngine ci = new ContextEngine();
        ci.setEnabled(true);

        ci.push("a1");
        ci.push("a11");
        ci.pop();
        ci.pop();

        waitUtilStatsGetCollected(ci);

        ci.getStatsEngine().process( stats -> {
            Map<String, Stat> map = stats.getMap();
            Assert.assertEquals(2, map.size());
            Assert.assertTrue(map.containsKey("a1"));
            Assert.assertTrue(map.containsKey("a11"));
            Assert.assertEquals(1, map.get("a1").getChildren().size());
            Assert.assertNotNull(map.get("a1").getChildren().get("a11"));
        });
    }

    private void waitUtilStatsGetCollected(ContextEngine ci) {
        for(int i=0; i< 10; i++){
            if( ci.getStatsEngine().getQueueSize() == 0 ){
                break;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    public void should_return_enabled(){
        PropertyTestUtil.withProperty(Properties.ENABLED, "true", ()->{
            Assert.assertTrue(new ContextEngine().isEnabled());
        });
    }

    @Test
    public void should_return_disabled(){
        PropertyTestUtil.withProperty(Properties.ENABLED, "false", ()->{
            Assert.assertFalse(new ContextEngine().isEnabled());
        });
    }
}