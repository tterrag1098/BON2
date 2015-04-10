package com.github.parker8283.bon2.gui;

import java.awt.Component;

import javax.swing.JOptionPane;

import com.github.parker8283.bon2.BON2Gui;
import com.github.parker8283.bon2.data.IErrorHandler;

public class GUIErrorHandler implements IErrorHandler {
    private Component parent;

    public GUIErrorHandler(Component parent) {
        this.parent = parent;
    }

    @Override
    public boolean handleError(String message, boolean warning) {
        JOptionPane.showMessageDialog(parent, message, BON2Gui.ERROR_DIALOG_TITLE, warning ? JOptionPane.WARNING_MESSAGE : JOptionPane.ERROR_MESSAGE);
        return true;
    }
}
