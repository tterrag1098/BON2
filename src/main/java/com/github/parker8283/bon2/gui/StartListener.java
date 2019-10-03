package com.github.parker8283.bon2.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import com.github.parker8283.bon2.BON2Gui;
import com.github.parker8283.bon2.BON2Impl;
import com.github.parker8283.bon2.util.MCPVersions;
import com.github.parker8283.bon2.util.MCPVersions.MCPVersion;
import com.github.parker8283.bon2.util.MappingVersions.MappingVersion;

import net.minecraftforge.srgutils.MinecraftVersion;

public class StartListener extends MouseAdapter {
    private BON2Gui parent;
    private Thread run = null;
    private JTextField input;
    private JTextField output;
    private JComboBox<MinecraftVersion> mcVer;
    private JComboBox<MappingVersion> mappingVer;
    private JLabel progressLabel;
    private JProgressBar progressBar;

    public StartListener(BON2Gui parent, JTextField input, JTextField output, JComboBox<MinecraftVersion> mcVer, JComboBox<MappingVersion> mappingVer, JLabel progressLabel, JProgressBar progressBar) {
        this.parent = parent;
        this.input = input;
        this.output = output;
        this.mcVer = mcVer;
        this.mappingVer = mappingVer;
        this.progressLabel = progressLabel;
        this.progressBar = progressBar;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(!input.getText().endsWith(".jar") || !output.getText().endsWith(".jar")) {
            JOptionPane.showMessageDialog(parent, "Nice try, but only JAR mods work.", BON2Gui.ERROR_DIALOG_TITLE, JOptionPane.ERROR_MESSAGE);
        }
        if(run != null && run.isAlive()) {
            return;
        }
        run = new Thread("BON2 Remapping Thread") {
            @Override
            public void run() {
                try {
                    MCPVersion mcp = MCPVersions.get((MinecraftVersion)mcVer.getSelectedItem());
                    MappingVersion map = (MappingVersion)mappingVer.getSelectedItem();

                    BON2Impl.remap(new File(input.getText()), new File(output.getText()), mcp, map, new GUIErrorHandler(parent), new GUIProgressListener(progressLabel, progressBar));
                } catch(Exception ex) {
                    JOptionPane.showMessageDialog(parent, "There was an error.\n" + ex.toString() + "\n" + getFormattedStackTrace(ex.getStackTrace()), BON2Gui.ERROR_DIALOG_TITLE, JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        run.start();
    }

    private String getFormattedStackTrace(StackTraceElement[] stacktrace) {
        StringBuilder sb = new StringBuilder();
        for(StackTraceElement element : stacktrace) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
