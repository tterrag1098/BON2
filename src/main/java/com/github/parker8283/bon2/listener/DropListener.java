package com.github.parker8283.bon2.listener;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.List;

/**
 * Created by clienthax on 11/12/2014.
 */
public class DropListener extends DropTarget {

    private JTextField textField;

    public DropListener(JTextField textField) {
        this.textField = textField;
    }

    public void drop(DropTargetDropEvent event) {
        try {
            event.acceptDrop(DnDConstants.ACTION_COPY);
            List<File> droppedFiles = (List<File>) event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
            for (File file : droppedFiles) {
                textField.setText(file.getAbsolutePath());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
