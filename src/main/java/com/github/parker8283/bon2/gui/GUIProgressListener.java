package com.github.parker8283.bon2.gui;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import com.github.parker8283.bon2.data.IProgressListener;

public class GUIProgressListener implements IProgressListener {
    private JLabel progressLabel;
    private JProgressBar progressBar;

    public GUIProgressListener(JLabel progressLabel, JProgressBar progressBar) {
        this.progressLabel = progressLabel;
        this.progressBar = progressBar;
    }

    @Override
    public void start(final int max, final String label) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressLabel.setText(label);
                if(progressBar.isIndeterminate()) {
                    progressBar.setIndeterminate(false);
                }
                if(max >= 0) {
                    progressBar.setMaximum(max);
                }
                progressBar.setValue(0);
            }
        });
    }

    @Override
    public void startWithoutProgress(final String label) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressLabel.setText(label);
                progressBar.setIndeterminate(true);
            }
        });
    }

    @Override
    public void setProgress(final int value) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setValue(value);
            }
        });
    }

    @Override
    public void setMax(final int max) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setMaximum(max);
            }
        });
    }
}
