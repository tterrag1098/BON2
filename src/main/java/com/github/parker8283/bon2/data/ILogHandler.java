package com.github.parker8283.bon2.data;

public interface ILogHandler {

    void debug(boolean doLogDebug, String message);

    void info(String message);

    void warn(String message, Throwable t);

    void error(String message, Throwable t);
}
