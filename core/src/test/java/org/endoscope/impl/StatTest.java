package org.endoscope.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

public class StatTest {

    @Test
    public void should_set_and_get(){
        Stat s = new Stat();
        s.setHits(13);
        Assert.assertEquals(13, s.getHits());

        s.setMax(14);
        Assert.assertEquals(14, s.getMax());

        s.setMin(15);
        Assert.assertEquals(15, s.getMin());

        s.setAvg(16);
        Assert.assertEquals(16, s.getAvg());

        s.setAh10(17);
        Assert.assertEquals(17, s.getAh10());

        Map m = new HashMap<>();
        s.setChildren(m);
        Assert.assertSame(m, s.getChildren());
    }


    @Test
    public void should_set_not_empty_children_if_null(){
        Stat s = new Stat();
        Assert.assertNull(s.getChildren());

        s.ensureChildrenMap();
        Assert.assertNotNull(s.getChildren());

        s.getChildren().put("x", null);

        s.ensureChildrenMap();
        Assert.assertNotNull(s.getChildren());
        Assert.assertTrue(s.getChildren().containsKey("x"));
    }

    @Test
    public void should_get_existing_child(){
        Stat s = new Stat();
        Stat child = s.getChild("x");
        Assert.assertNull(child);

        child = s.createChild("x");
        Assert.assertNotNull(s.getChildren());

        Stat child2 = s.getChild("x");
        Assert.assertSame(child, child2);
    }

    @Test
    public void should_update_stat(){
        Stat s = new Stat();

        s.update(10);
        s.updateAvgHits(100);

        Assert.assertEquals(10, s.getMax());
        Assert.assertEquals(10, s.getMin());
        Assert.assertEquals(10, s.getAvg(), 0.00001);
        Assert.assertEquals(1, s.getHits());
        Assert.assertEquals(1000, s.getAh10());
        Assert.assertEquals(100, s.avgParent, 0.00001);

        s.update(20);
        s.updateAvgHits(200);

        Assert.assertEquals(20, s.getMax());
        Assert.assertEquals(10, s.getMin());
        Assert.assertEquals(15, s.getAvg(), 0.00001);
        Assert.assertEquals(2, s.getHits());
        Assert.assertEquals(1500, s.getAh10());
        Assert.assertEquals(150, s.avgParent, 0.00001);
    }

    @Test
    public void should_not_loose_precision(){
        Stat s = new Stat();
        Random random = new Random();

        IntStream.range(0, 100000000).forEach( i -> {
            long r = random.nextInt(1000);
            s.update(r);
            s.updateAvgHits(r);
        });

        System.out.println("max: " + s.getMax());
        System.out.println("min: " + s.getMin());
        System.out.println("avg: " + s.getAvg());
        System.out.println("hits: " + s.getHits());
        System.out.println("getAh10(): " + s.getAh10());
        System.out.println("avgParent: " + s.avgParent);

        //with such amount of samples we should be around the 500 - accept 0.5% difference
        Assert.assertTrue(s.getMax() > 995);
        Assert.assertTrue(s.getMin() < 5);
        Assert.assertTrue(s.getAvg() < 505 && s.getAvg() > 445);
        Assert.assertEquals(100000000, s.getHits());
        Assert.assertTrue(s.getAh10() < 5050 && s.getAh10() > 4450 );
        Assert.assertTrue(s.avgParent < 505 && s.avgParent > 445);
    }
}