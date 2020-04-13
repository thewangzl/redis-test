package com.thewangzl.redistest.util;

import org.apache.commons.lang.RandomStringUtils;

public class RandomUtil {

    public static String randomString(){
        return String.valueOf(System.currentTimeMillis()) + RandomStringUtils.randomNumeric(10);
    }
}
