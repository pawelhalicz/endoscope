/*
 * Copyright (c) 2016 SmartRecruiters Inc. All Rights Reserved.
 */

package org.endoscope.example;

import java.util.Random;

/**
 * Date: 11/02/16
 * Time: 21:32
 *
 * @author p.halicz
 */
public class SleepUtil {
    private static Random r = new Random();
    public static void randomSleep() {
        try {
            Thread.sleep(r.nextInt(10));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
