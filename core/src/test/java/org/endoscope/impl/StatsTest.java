package org.endoscope.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static org.endoscope.impl.PropertyTestUtil.withProperty;

public class StatsTest {
    final ObjectMapper om;

    public StatsTest(){
        om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.enable(SerializationFeature.INDENT_OUTPUT);
    }

    private String toJson(Object o){
        try {
            return om.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T fromJson(InputStream src, Class<T> clazz){
        try {
            return om.readValue(src, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T fromResourceJson(String resourceName, Class<T> clazz){
        InputStream src = this.getClass().getResourceAsStream(resourceName);
        try{
            return fromJson(src, clazz);
        }finally{
            IOUtils.closeQuietly(src);
        }
    }

    private String getResourceString(String resourceName){
        InputStream src = this.getClass().getResourceAsStream(resourceName);
        try {
            return IOUtils.toString(src);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally{
            IOUtils.closeQuietly(src);
        }
    }

    private void process(String input, String output) {
        Context context = fromResourceJson(input, Context.class);

        Stats stats = new Stats();
        stats.store(context);

        String result = toJson(stats.getMap());

        String expected = getResourceString(output);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void should_collect_stats_1(){
        process("/input1.json", "/expected1.json");
    }

    @Test
    public void should_collect_stats_2(){
        process("/input2.json", "/expected2.json");
    }

    @Test
    public void should_collect_stats_3(){
        process("/input3.json", "/expected3.json");
    }

    @Test
    public void should_collect_stats_5_avg_parent_hits(){
        process("/input5.json", "/expected5.json");
    }

    @Test
    public void should_limit_number_of_stats(){
        //stats over limit will be ignored
        withProperty(Properties.MAX_STAT_COUNT, "2", ()->{
            process("/input4.json", "/expected4.json");
        });
    }

    //estimate stats size
    @Ignore
    @Test
    public void estimate_stats_size(){
        withProperty(Properties.MAX_STAT_COUNT, "10000000", ()->{
            System.gc();
            long before = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/(1024*1024);
            System.out.println("Before: " + before + " MB");
            Stats stats = new Stats();
            for( long i=0; i<1000001; i++){
                if( i % 100000 == 0 ){
                    System.gc();
                    System.out.println(i + " ~ " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/(1024*1024) - before) + " MB" );
                }
                stats.store(new Context("" + i, 1L));
            }

            Assert.assertEquals(stats.getMap().size(), 1000001);//make sure we didn't hit the limit
        });
    }

    //estimate json doc size
    @Ignore
    @Test
    public void estimate_json_stats_size(){
        ObjectMapper om = new ObjectMapper();
        withProperty(Properties.MAX_STAT_COUNT, "10000000", ()->{
            Stats stats = new Stats();
            for( long i=0; i<1000001; i++){
                if( i % 100000 == 0 ){
                    final long ii = i;
                    try{
                        File out = File.createTempFile("endoscope-tmp", ".json");
                        om.writeValue(out, stats.getMap());
                        System.out.println( ii + " ~ " + (out.length()/(1024*1024)) + " MB");
                        out.delete();
                    }catch(IOException e){
                        throw new RuntimeException(e);
                    }
                }
                stats.store(new Context("" + i, 1L));
            }
            Assert.assertEquals(1000001, stats.getMap().size());//make sure we didn't hit the limit
        });
    }

    @Test
    public void should_increment_lost(){
        Stats s = new Stats();
        Assert.assertEquals(0, s.getLost());
        s.incrementLost();
        Assert.assertEquals(1, s.getLost());
    }

    @Test
    public void should_set_error_message(){
        Stats s = new Stats();
        Assert.assertNull(s.getFatalError());
        s.setFatalError("error");
        Assert.assertEquals("error", s.getFatalError());
    }

    @Test
    public void should_get_stats_left(){
        Stats s = new Stats();
        Assert.assertEquals(Properties.getMaxStatCount(), s.getStatsLeft());

        Context parent = new Context("id", 13);
        parent.addChild(new Context("id2", 133));
        s.store(parent);
        
        Assert.assertEquals(Properties.getMaxStatCount() - 3, s.getStatsLeft());
    }
}