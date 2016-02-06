package org.endoscope.example;

import javax.inject.Inject;

public class TheService {
    @Inject
    TheBean service1;

    @Inject
    TheApplicationScoped service2;

    public String process(int level) {
        level--;
        for( int i=0; i<level; i++){
            service1.process(level);
            service2.process(level);
        }
        return "OK";
    }
}
