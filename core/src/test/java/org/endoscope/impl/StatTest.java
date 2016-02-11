package org.endoscope.impl;

import java.util.Random;
import java.util.stream.IntStream;

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
        Assert.assertEquals(10, s.avg, 0.00001);
        Assert.assertEquals(1, s.hits);
        Assert.assertEquals(1000, s.getAh10());
        Assert.assertEquals(100, s.avgParent, 0.00001);

        s.update(20);
        s.updateAvgHits(200);

        Assert.assertEquals(20, s.max);
        Assert.assertEquals(10, s.min);
        Assert.assertEquals(15, s.avg, 0.00001);
        Assert.assertEquals(2, s.hits);
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

        System.out.println("max: " + s.max);
        System.out.println("min: " + s.min);
        System.out.println("avg: " + s.avg);
        System.out.println("hits: " + s.hits);
        System.out.println("getAh10(): " + s.getAh10());
        System.out.println("avgParent: " + s.avgParent);

        //with such amount of samples we should be around the 500 - accept 0.5% difference
        Assert.assertTrue(s.max > 995);
        Assert.assertTrue(s.min < 5);
        Assert.assertTrue(s.avg < 505 && s.avg > 445);
        Assert.assertEquals(100000000, s.hits);
        Assert.assertTrue(s.getAh10() < 5050 && s.getAh10() > 4450 );
        Assert.assertTrue(s.avgParent < 505 && s.avgParent > 445);
    }
}