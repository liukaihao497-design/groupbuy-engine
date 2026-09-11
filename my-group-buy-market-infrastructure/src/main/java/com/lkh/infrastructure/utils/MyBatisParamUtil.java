package com.lkh.infrastructure.utils;

public class MyBatisParamUtil {

    public static boolean isNotEmpty(Object value) {
        if (value == null) {
            return false;
        }

        if (value instanceof String) {
            return !((String) value).trim().isEmpty();
        }

        return true;
    }
}