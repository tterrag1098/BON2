package com.github.parker8283.bon2.data;

public interface IProgressListener {

    void start(int max, String label);

    void startWithoutProgress(String label);

    void setProgress(int value);

    void setMax(int max);
}
