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
    public void should_get_child_or_create_new(){
        Stat s = new Stat();
        Stat child = s.getOrAddChild("x");
        Assert.assertNotNull(s.children);

        Stat child2 = s.getOrAddChild("x");
        Assert.assertSame(child, child2);
    }

    @Test
    public void should_update_stat(){
        Stat s = new Stat();

        s.update(10);
        s.updateParentAvgCount(100);

        Assert.assertEquals(10, s.max);
        Assert.assertEquals(10, s.min);
        Assert.assertEquals(10, s.avg);
        Assert.assertEquals(1, s.count);
        Assert.assertEquals(100, s.parentAvgCount);

        s.update(20);
        s.updateParentAvgCount(200);

        Assert.assertEquals(20, s.max);
        Assert.assertEquals(10, s.min);
        Assert.assertEquals(15, s.avg);
        Assert.assertEquals(2, s.count);
        Assert.assertEquals(150, s.parentAvgCount);
    }
}