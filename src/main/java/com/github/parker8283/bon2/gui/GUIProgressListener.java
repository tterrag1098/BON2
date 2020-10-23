package com.github.parker8283.bon2.gui;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import com.github.parker8283.bon2.cli.CLIProgressListener;

public class GUIProgressListener extends CLIProgressListener {
    private JLabel progressLabel;
    private JProgressBar progressBar;

    public GUIProgressListener(JLabel progressLabel, JProgressBar progressBar) {
        this.progressLabel = progressLabel;
        this.progressBar = progressBar;
    }

    @Override
    public void start(final int max, final String label) {
        super.start(max, label);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressLabel.setText(getFormatedText(label));
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
        super.startWithoutProgress(label);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressLabel.setText(getFormatedText(label));
                progressBar.setIndeterminate(true);
            }
        });
    }

    @Override
    public void setProgress(final int value) {
        super.setProgress(value);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setValue(value);
            }
        });
    }

    @Override
    public void setMax(final int max) {
        super.setMax(max);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setMaximum(max);
            }
        });
    }

    @Override
    public void setLabel(String label) {
        super.setLabel(label);
        SwingUtilities.invokeLater(() -> progressLabel.setText(getFormatedText(label)));
    }

    private String getFormatedText(String value) {
        return "<html>" + value + "</html>"; //This prevents the window from being resized when the text is long.
    }
}
