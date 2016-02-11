/*
 * Copyright (c) 2016 SmartRecruiters Inc. All Rights Reserved.
 */

package org.endoscope.impl;

/**
 * Date: 11/02/16
 * Time: 23:24
 *
 * @author p.halicz
 */
public class PropertyTestUtil {

    public static void withProperty(String name, String value, Runnable runnable) {
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
}
