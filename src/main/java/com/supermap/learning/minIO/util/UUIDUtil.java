package com.supermap.learning.minIO.util;

import java.util.UUID;

/**
 * @author lty
 */
public class UUIDUtil {

    public static String get() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
