package com.github.parker8283.bon2.cli;

import com.github.parker8283.bon2.data.ILogHandler;
import com.github.parker8283.bon2.data.IProgressListener;

public class CLIProgressListener implements IProgressListener {
    private final ILogHandler log;

    public CLIProgressListener(ILogHandler log) {
        this.log = log;
    }

    @Override
    public void start(int max, String label) {
        log.info(label);
    }

    @Override
    public void startWithoutProgress(String label) {
        log.info(label);
    }

    @Override
    public void setProgress(int value) {
        //NO-OP
    }

    @Override
    public void setMax(int max) {
        //NO-OP
    }
}
