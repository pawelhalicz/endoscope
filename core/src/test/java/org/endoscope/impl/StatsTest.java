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

        final String[] result = new String[1];
        stats.process(map -> result[0] = toJson(map) );

        String expected = getResourceString(output);
        Assert.assertEquals(expected, result[0]);
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
    public void should_limit_number_of_stats(){
        //stats over limit will be ignored
        withProperty("endoscope.max.stat.count", "2", ()->{
            process("/input4.json", "/expected4.json");
        });
    }

    private void withProperty(String name, String value, Runnable runnable) {
        String previousValue = System.getProperty(name);
        System.setProperty(name, value);
        try{
            runnable.run();
        }finally{
            if( previousValue == null ){
                System.clearProperty(name);
            } else {
                System.setProperty(name, previousValue);
            }
        }
    }

    //estimate stats size
    @Ignore
    @Test
    public void estimate_stats_size(){
        withProperty("endoscope.max.stat.count", "10000000", ()->{
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
            stats.process( map -> {
                Assert.assertEquals(map.size(), 1000001);//make sure we didn't hit the limit
            });
        });
    }

    //estimate json doc size
    @Ignore
    @Test
    public void estimate_json_stats_size(){
        ObjectMapper om = new ObjectMapper();
        withProperty("endoscope.max.stat.count", "10000000", ()->{
            Stats stats = new Stats();
            for( long i=0; i<1000001; i++){
                if( i % 100000 == 0 ){
                    final long ii = i;
                    stats.process(map -> {
                        try{
                            File out = File.createTempFile("endoscope-tmp", ".json");
                            om.writeValue(out, map);
                            System.out.println( ii + " ~ " + (out.length()/(1024*1024)) + " MB");
                            out.delete();
                        }catch(IOException e){
                            throw new RuntimeException(e);
                        }
                    });
                }
                stats.store(new Context("" + i, 1L));
            }
            stats.process( map -> {
                Assert.assertEquals(map.size(), 1000001);//make sure we didn't hit the limit
            });
        });
    }
}