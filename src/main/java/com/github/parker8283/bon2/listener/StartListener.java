package com.github.parker8283.bon2.listener;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.*;

import com.github.parker8283.bon2.data.BONFiles;
import com.github.parker8283.bon2.data.IProgressListener;
import com.github.parker8283.bon2.srg.ClassCollection;
import com.github.parker8283.bon2.srg.Repo;
import com.github.parker8283.bon2.util.JarUtils;
import com.github.parker8283.bon2.util.Remapper;
import com.google.common.base.Strings;

public class StartListener extends MouseAdapter {
    private Thread run = null;
    private JTextField input;
    private JTextField output;
    private JComboBox forgeVer;
    private JLabel progressLabel;
    private JProgressBar progressBar;
    private ClassCollection inputCC;
    private ClassCollection outputCC;

    public StartListener(JTextField input, JTextField output, JComboBox forgeVer, JLabel progressLabel, JProgressBar progressBar) {
        this.input = input;
        this.output = output;
        this.forgeVer = forgeVer;
        this.progressLabel = progressLabel;
        this.progressBar = progressBar;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(!input.getText().endsWith(".jar") || !output.getText().endsWith(".jar")) {
            throw new RuntimeException("You were being an idiot and changed the extension of one of the jars. Don't.");
        }
        if(run != null && run.isAlive()) {
            return;
        }
        run = new Thread("BON2 Remapping Thread") {
            @Override
            public void run() {
                IProgressListener progress = new IProgressListener() {
                    private String currentText;

                    @Override
                    public void start(final int max, final String label) {
                        currentText = Strings.isNullOrEmpty(label) ? "" : label;
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                progressLabel.setText(label);
                                if(max >= 0) {
                                    progressBar.setMaximum(max);
                                }
                                progressBar.setValue(0);
                            }
                        });
                    }

                    @Override
                    public void startWithoutProgress(final String label) {
                        currentText = Strings.isNullOrEmpty(label) ? "" : label;
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                progressLabel.setText(label);
                                progressBar.setMaximum(1);
                                progressBar.setValue(1);
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
                };

                File srgsFolder = new File(BONFiles.MINECRAFTFORGE_FORGE_FOLDER, forgeVer.getSelectedItem().toString() + File.separator + "srgs");
                try {
                    Repo.loadMappings(srgsFolder, progress);
                    inputCC = JarUtils.readFromJar(new File(input.getText()), progress);
                    outputCC = Remapper.remap(inputCC, progress);
                    JarUtils.writeToJar(outputCC, new File(output.getText()), progress);
                    progress.startWithoutProgress("Done!");
                } catch(Exception ex) {
                    throw new RuntimeException("There was an error.", ex);
                }
            }
        };
        run.start();
    }
}
