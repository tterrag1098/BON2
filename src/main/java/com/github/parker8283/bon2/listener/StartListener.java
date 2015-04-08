package com.github.parker8283.bon2.listener;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.*;

import com.github.parker8283.bon2.BON2Gui;
import com.github.parker8283.bon2.data.BONFiles;
import com.github.parker8283.bon2.data.IProgressListener;
import com.github.parker8283.bon2.srg.ClassCollection;
import com.github.parker8283.bon2.srg.Repo;
import com.github.parker8283.bon2.util.JarUtils;
import com.github.parker8283.bon2.util.Remapper;
import com.google.common.base.Strings;

public class StartListener extends MouseAdapter {
    private Component parent;
    private Thread run = null;
    private JTextField input;
    private JTextField output;
    private JComboBox forgeVer;
    private JLabel progressLabel;
    private JProgressBar progressBar;
    private ClassCollection inputCC;
    private ClassCollection outputCC;

    public StartListener(Component parent, JTextField input, JTextField output, JComboBox forgeVer, JLabel progressLabel, JProgressBar progressBar) {
        this.parent = parent;
        this.input = input;
        this.output = output;
        this.forgeVer = forgeVer;
        this.progressLabel = progressLabel;
        this.progressBar = progressBar;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        assert parent instanceof BON2Gui : "Parent component must be an instance of BON2";
        ((BON2Gui)parent).prefs.put(BON2Gui.PREFS_KEY_FORGEVER, forgeVer.getSelectedItem().toString());
        if(!input.getText().endsWith(".jar") || !output.getText().endsWith(".jar")) {
            JOptionPane.showMessageDialog(parent, "Nice try, but only JAR mods work.", BON2Gui.ERROR_DIALOG_TITLE, JOptionPane.ERROR_MESSAGE);
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
                        currentText = Strings.isNullOrEmpty(label) ? "" : label;
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
                };

                File srgsFolder = getSrgsFolder(forgeVer);
                try {
                    Repo.loadMappings(srgsFolder, progress);
                    inputCC = JarUtils.readFromJar(parent, new File(input.getText()), progress);
                    outputCC = Remapper.remap(inputCC, progress);
                    JarUtils.writeToJar(outputCC, new File(output.getText()), progress);
                    progress.start(1, "Done!");
                    progress.setProgress(1);
                } catch(Exception ex) {
                    JOptionPane.showMessageDialog(parent, "There was an error.\n" + ex.toString() + "\n" + getFormattedStackTrace(ex.getStackTrace()), BON2Gui.ERROR_DIALOG_TITLE, JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        run.start();
    }

    private File getSrgsFolder(JComboBox comboBox) {
        String selectedVer = comboBox.getSelectedItem().toString();
        if(selectedVer.endsWith("shipped")) {
            return new File(BONFiles.MINECRAFTFORGE_FORGE_FOLDER, selectedVer.replace("-shipped", "") + File.separator + "srgs");
        } else {
            String forgeVer = selectedVer.substring(0, selectedVer.lastIndexOf('-'));
            String mappingChan = selectedVer.substring(selectedVer.lastIndexOf('-') + 1, selectedVer.lastIndexOf('_'));
            String mappingVer = selectedVer.substring(selectedVer.lastIndexOf('_') + 1);
            return new File(BONFiles.MINECRAFTFORGE_FORGE_FOLDER, forgeVer + File.separator + mappingChan + File.separator + mappingVer + File.separator + "srgs");
        }
    }

    private String getFormattedStackTrace(StackTraceElement[] stacktrace) {
        StringBuilder sb = new StringBuilder();
        for(StackTraceElement element : stacktrace) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
