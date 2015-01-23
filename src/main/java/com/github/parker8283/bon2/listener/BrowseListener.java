package com.github.parker8283.bon2.listener;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

public class BrowseListener extends MouseAdapter {
    private Component parent;
    private boolean isOpen;
    private JTextField field;
    private JFileChooser fileChooser;

    public BrowseListener(Component parent, boolean isOpen, JTextField field) {
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
        fileChooser.setCurrentDirectory(new File(Paths.get("").toAbsolutePath().getParent().toString()));
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
        }
    }
}
