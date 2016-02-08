package org.endoscope.impl;

import org.junit.Assert;
import org.junit.Test;

public class StatTest {

    @Test
    public void should_set_not_empty_children_if_null(){
        Stat s = new Stat();
        Assert.assertNull(s.children);

        s.ensureChildrenMap();
        Assert.assertNotNull(s.children);

        s.children.put("x", null);

        s.ensureChildrenMap();
        Assert.assertNotNull(s.children);
        Assert.assertTrue(s.children.containsKey("x"));
    }

    @Test
    public void should_get_existing_child(){
        Stat s = new Stat();
        Stat child = s.getChild("x");
        Assert.assertNull(child);

        child = s.createChild("x");
        Assert.assertNotNull(s.children);

        Stat child2 = s.getChild("x");
        Assert.assertSame(child, child2);
    }

    @Test
    public void should_update_stat(){
        Stat s = new Stat();

        s.update(10);
        s.updateAvgHits(100);

        Assert.assertEquals(10, s.max);
        Assert.assertEquals(10, s.min);
        Assert.assertEquals(10, s.avg);
        Assert.assertEquals(1, s.hits);
        Assert.assertEquals(1000, s.ah10);

        s.update(20);
        s.updateAvgHits(200);

        Assert.assertEquals(20, s.max);
        Assert.assertEquals(10, s.min);
        Assert.assertEquals(15, s.avg);
        Assert.assertEquals(2, s.hits);
        Assert.assertEquals(1500, s.ah10);
    }
}