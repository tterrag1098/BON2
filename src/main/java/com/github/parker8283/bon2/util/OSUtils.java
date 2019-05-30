//MrPyro 2019
package com.github.parker8283.bon2.util;

import java.util.Locale;

public class OSUtils {
    public enum OS {
        Windows,
        Mac,
        Linux
    }
    
    private static String OSProp = System.getProperty("os.name").toLowerCase(Locale.ROOT);
    
    private static OS current = null;
    
    public static OS getOS() {
        if (current == null) {
            if (OSProp.contains("linux")) {
                current = OS.Linux;
            } else if (OSProp.contains("mac")) {
                current = OS.Mac;
            } else {
                current = OS.Windows;
            }
        }
        return current;
    }
}
