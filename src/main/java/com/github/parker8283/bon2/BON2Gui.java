package com.github.parker8283.bon2;

import java.awt.Dimension;
import java.io.IOException;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

import com.github.parker8283.bon2.data.GuiDownloadNew;
import com.github.parker8283.bon2.data.MappingVersion;
import com.github.parker8283.bon2.data.VersionLookup;
import com.github.parker8283.bon2.gui.BrowseListener;
import com.github.parker8283.bon2.gui.RefreshListener;
import com.github.parker8283.bon2.gui.StartListener;
import com.google.common.collect.Lists;

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.prefs.Preferences;

public class BON2Gui extends JFrame {

    public static final String ERROR_DIALOG_TITLE = "Error - BON2";
    public static final String PREFS_KEY_FORGEVER = "forgeVer";
    public static final String PREFS_KEY_OPEN_LOC = "openLoc";
    public static final String PREFS_KEY_SAVE_LOC = "closeLoc";

    private static final long serialVersionUID = -619289399889088924L;

    public final Preferences prefs = Preferences.userNodeForPackage(BON2Gui.class);

    private JPanel contentPane;
    private JTextField inputJarLoc;
    private JLabel lblOutput;
    private JTextField outputJarLoc;
    private JButton btnBrouseOutput;
    private JLabel lblForgeVer;
    private JLabel lblProgressText;
    private JProgressBar masterProgress;

    public BON2Gui() {
        setMinimumSize(new Dimension(550, 210));
        setTitle("BON2");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(100, 100, 550, 210);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        JLabel lblInput = new JLabel("Input JAR");
        lblInput.setHorizontalAlignment(SwingConstants.CENTER);

        inputJarLoc = new JTextField();
        inputJarLoc.setColumns(10);
        inputJarLoc.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_REFERENCE);
                    List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (droppedFiles.size() == 1){
                        inputJarLoc.setText(droppedFiles.get(0).getAbsolutePath());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        JButton btnBrouseInput = new JButton("Browse");
        btnBrouseInput.addMouseListener(new BrowseListener(this, true, inputJarLoc));

        lblOutput = new JLabel("Output JAR");
        lblOutput.setHorizontalAlignment(SwingConstants.CENTER);

        outputJarLoc = new JTextField();
        outputJarLoc.setColumns(10);
        outputJarLoc.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_REFERENCE);
                    List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (droppedFiles.size() == 1){
                        outputJarLoc.setText(droppedFiles.get(0).getAbsolutePath());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        btnBrouseOutput = new JButton("Browse");
        btnBrouseOutput.addMouseListener(new BrowseListener(this, false, outputJarLoc));

        lblForgeVer = new JLabel("Mappings");
        lblForgeVer.setHorizontalAlignment(SwingConstants.CENTER);

        JComboBox<MappingVersion> forgeVersions = new JComboBox<MappingVersion>();

        JButton btnRefreshVers = new JButton("Refresh");
        RefreshListener refresh = new RefreshListener(this, forgeVersions);
        btnRefreshVers.addMouseListener(refresh);
        try {
            VersionLookup.INSTANCE.refresh(); // make sure we've queried the json, as this will halt the main thread
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not load MCP versions from web, mapping versions may be incomplete.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
        refresh.mouseClicked(null); // update the versions initially
        String forgeVer = prefs.get(PREFS_KEY_FORGEVER, "");
        for (MappingVersion m : comboBoxToList(forgeVersions)) {
            if (m.getVersion().contains(forgeVer)) {
                forgeVersions.setSelectedItem(m);
            }
        }
        
        // Add this after previously saved value is set
        forgeVersions.addActionListener(e -> {
            Object selected = forgeVersions.getSelectedItem();
            if (selected == null) {
                prefs.remove(BON2Gui.PREFS_KEY_FORGEVER);
            } else {
                prefs.put(BON2Gui.PREFS_KEY_FORGEVER, selected.toString());
            }
        });
        
        JButton buttonDownload = new JButton("Download");
        buttonDownload.addActionListener(e -> {
            GuiDownloadNew gui;
            try {
                gui = new GuiDownloadNew(refresh);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(BON2Gui.this, ex, "Could not load mappings", JOptionPane.ERROR_MESSAGE);
                return;
            }
            gui.setLocation(getLocation());
            gui.setVisible(true);
        });

        masterProgress = new JProgressBar();

        lblProgressText = new JLabel("Ready!");

        JButton btnStart = new JButton("Go!");
        btnStart.addMouseListener(new StartListener(this, inputJarLoc, outputJarLoc, forgeVersions, lblProgressText, masterProgress));

        lblProgressText.setHorizontalAlignment(SwingConstants.CENTER);

        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane.setHorizontalGroup(
            gl_contentPane.createParallelGroup(Alignment.TRAILING)
                .addGroup(gl_contentPane.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                        .addComponent(lblProgressText, GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
                        .addComponent(btnStart, GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
                        .addGroup(gl_contentPane.createSequentialGroup()
                            .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false)
                                .addComponent(lblForgeVer, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblInput, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblOutput, GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE))
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                                .addComponent(inputJarLoc, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
                                .addComponent(outputJarLoc, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
                                .addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
                                    .addComponent(forgeVersions, 0, 225, Short.MAX_VALUE)
                                    .addPreferredGap(ComponentPlacement.RELATED)
                                    .addComponent(buttonDownload, GroupLayout.PREFERRED_SIZE, 91, GroupLayout.PREFERRED_SIZE)))
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING, false)
                                .addComponent(btnRefreshVers, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnBrouseOutput, GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
                                .addComponent(btnBrouseInput, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addComponent(masterProgress, GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE))
                    .addContainerGap())
        );
        gl_contentPane.setVerticalGroup(
            gl_contentPane.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_contentPane.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
                        .addComponent(inputJarLoc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnBrouseInput)
                        .addComponent(lblInput))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
                        .addComponent(lblOutput)
                        .addComponent(outputJarLoc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnBrouseOutput))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
                        .addComponent(lblForgeVer)
                        .addComponent(btnRefreshVers)
                        .addComponent(buttonDownload)
                        .addComponent(forgeVersions, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(btnStart)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(masterProgress, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addGap(7)
                    .addComponent(lblProgressText)
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        
        contentPane.setLayout(gl_contentPane);
    }

    public JTextField getOutputField() {
        return outputJarLoc;
    }

    private <T> List<T> comboBoxToList(JComboBox<T> comboBox) {
        List<T> ret = Lists.newArrayList();
        for(int i = 0; i < comboBox.getItemCount(); i++) {
            ret.add(comboBox.getItemAt(i));
        }
        return ret;
    }
}
