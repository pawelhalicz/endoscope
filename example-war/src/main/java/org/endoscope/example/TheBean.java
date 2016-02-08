package org.endoscope.example;

import javax.inject.Inject;

public class TheBean {
    @Inject
    TheStateless service;

    public String randomTimes(int level) {
        level--;
        for(int i = 0; i< System.currentTimeMillis() % 100; i++){
            service.process(level);
        }
        return "OK";
    }
}
