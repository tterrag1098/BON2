package com.github.parker8283.bon2.gui;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JOptionPane;

@SuppressWarnings("serial")
public class JarDropTarget extends DropTarget {

    private final Component parent;
    private final Consumer<String> action;

    public JarDropTarget(Component parent, Consumer<String> action) {
        this.parent = parent;
        this.action = action;
    }

    @Override
    public synchronized void drop(DropTargetDropEvent dtde) {
        try {
            dtde.acceptDrop(DnDConstants.ACTION_REFERENCE);
            @SuppressWarnings("unchecked")
            List<File> droppedFiles = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
            if (droppedFiles.size() == 1){
                String target = droppedFiles.get(0).getAbsolutePath();
                if (target.endsWith(".jar")) {
                    action.accept(target);
                } else {
                    JOptionPane.showMessageDialog(parent, "Only JAR files are supported.", "Error dropping file", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, ex, "Error dropping file", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
