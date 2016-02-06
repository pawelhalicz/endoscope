package org.endoscope.example;

import javax.inject.Inject;

public class TheBean {
    @Inject
    TheStateless service;

    public String process(int level) {
        level--;
        for( int i=0; i<level; i++){
            service.process(level);
        }
        return "OK";
    }
}
