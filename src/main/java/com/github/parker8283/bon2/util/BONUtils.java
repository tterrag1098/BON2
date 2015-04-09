package com.github.parker8283.bon2.util;

import java.util.List;

public class BONUtils {

    //Yes I know, this doesn't need a method. Bleh.
    public static boolean isValidMappingsVer(List<String> validMappings, String candidate) {
        return validMappings.contains(candidate);
    }
}
