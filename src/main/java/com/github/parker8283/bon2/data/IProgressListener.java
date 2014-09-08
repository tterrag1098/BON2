package com.github.parker8283.bon2.data;

public interface IProgressListener {

    public void start(int max, String label);

    public void startWithoutProgress(String label);

    public void setProgress(int value);

    public void setMax(int max);
}
