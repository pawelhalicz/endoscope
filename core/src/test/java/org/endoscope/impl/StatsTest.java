package org.endoscope.impl;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
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
}