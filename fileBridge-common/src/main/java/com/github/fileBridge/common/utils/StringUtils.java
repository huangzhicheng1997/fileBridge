package com.github.fileBridge.common.utils;

public class StringUtils {
    public static final String EMPTY = "";
    public static final String LINE = "\n";

    public static final byte LINE_BYTE = '\n';


    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
}
