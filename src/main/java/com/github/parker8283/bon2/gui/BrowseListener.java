package com.github.parker8283.bon2.gui;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import com.github.parker8283.bon2.BON2Gui;

public class BrowseListener extends MouseAdapter {
    private Component parent;
    private boolean isOpen;
    private JTextField field;
    private JFileChooser fileChooser;

    public BrowseListener(Component parent, boolean isOpen, JTextField field) {
        assert parent instanceof BON2Gui : "Parent component must be an instance of BON2Gui";
        this.parent = parent;
        this.isOpen = isOpen;
        this.field = field;
        this.fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileHidingEnabled(false);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                String fileName = f.getName();
                return f.isDirectory() || fileName.endsWith(".jar");
            }

            @Override
            public String getDescription() {
                return "JAR mods only";
            }
        });
        File currentDir = Paths.get("").toAbsolutePath().toFile();
        while (!currentDir.isDirectory()) {
            currentDir = currentDir.getParentFile();
        }
        fileChooser.setCurrentDirectory(currentDir);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int returnState;
        if(isOpen) {
            returnState = fileChooser.showOpenDialog(parent);
        } else {
            returnState = fileChooser.showSaveDialog(parent);
        }
        if(returnState == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                field.setText(file.getCanonicalPath());
            } catch(IOException ex) {
                field.setText(file.getAbsolutePath());
            }
            if(isOpen) {
                try {
                    ((BON2Gui)parent).getOutputField().setText(file.getCanonicalPath().replace(".jar", "-deobf.jar"));
                } catch(IOException ex) {
                    ((BON2Gui)parent).getOutputField().setText(file.getAbsolutePath().replace(".jar", "-deobf.jar"));
                }
            }
        }
    }
}
