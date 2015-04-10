package com.github.parker8283.bon2.cli;

import com.github.parker8283.bon2.data.IProgressListener;

public class CLIProgressListener implements IProgressListener {

    @Override
    public void start(int max, String label) {
        System.out.println(label);
    }

    @Override
    public void startWithoutProgress(String label) {
        System.out.println(label);
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
