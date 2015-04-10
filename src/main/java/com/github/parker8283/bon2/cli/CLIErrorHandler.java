package com.github.parker8283.bon2.cli;

import com.github.parker8283.bon2.data.IErrorHandler;

public class CLIErrorHandler implements IErrorHandler {

    @Override
    public boolean handleError(String message, boolean warning) {
        System.err.println(message);
        return true;
    }
}
