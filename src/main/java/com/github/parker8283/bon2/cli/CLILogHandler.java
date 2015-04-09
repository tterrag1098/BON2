package com.github.parker8283.bon2.cli;

import com.github.parker8283.bon2.data.ILogHandler;

public class CLILogHandler implements ILogHandler {

    @Override
    public void debug(boolean doLogDebug, String message, Throwable t) {
        if(doLogDebug) {
            System.out.println("[BON2] [DEBUG] " + message);
            if(t != null) {
                t.printStackTrace(System.out);
            }
        }
    }

    @Override
    public void info(String message, Throwable t) {
        System.out.println("[BON2] [INFO] " + message);
        if(t != null) {
            t.printStackTrace(System.out);
        }
    }

    @Override
    public void warn(String message, Throwable t) {
        System.err.println("[BON2] [WARN] " + message);
        if(t != null) {
            t.printStackTrace();
        }
    }

    @Override
    public void error(String message, Throwable t) {
        System.err.println("[BON2] [ERROR] " + message);
        if(t != null) {
            t.printStackTrace();
        }
    }
}
