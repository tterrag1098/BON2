package com.github.parker8283.bon2.gui;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import com.github.parker8283.bon2.BON2Gui;
import com.github.parker8283.bon2.data.BONFiles;
import com.github.parker8283.bon2.util.BONUtils;

public class RefreshListener extends MouseAdapter {
    private Component parent;
    private JComboBox comboBox;

    public RefreshListener(Component parent, JComboBox comboBox) {
        this.parent = parent;
        this.comboBox = comboBox;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(!BONFiles.USER_GRADLE_FOLDER.exists()) {
            JOptionPane.showMessageDialog(parent, "No user .gradle folder found. You must run ForgeGradle at least once in order to use this tool.", BON2Gui.ERROR_DIALOG_TITLE, JOptionPane.ERROR_MESSAGE);
        } else if(!BONFiles.USER_GRADLE_FOLDER.isDirectory()) {
            JOptionPane.showMessageDialog(parent, "The user .gradle isn't a folder. Delete it and try again.", BON2Gui.ERROR_DIALOG_TITLE, JOptionPane.ERROR_MESSAGE);
        }

        comboBox.removeAllItems();
        for(String version : BONUtils.buildValidMappings()) {
            //noinspection unchecked
            comboBox.addItem(version);
        }
    }
}
