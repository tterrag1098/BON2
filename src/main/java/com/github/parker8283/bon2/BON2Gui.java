package com.github.parker8283.bon2;

import java.awt.Dimension;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

import com.github.parker8283.bon2.data.MappingVersion;
import com.github.parker8283.bon2.data.VersionLookup;
import com.github.parker8283.bon2.gui.BrowseListener;
import com.github.parker8283.bon2.gui.RefreshListener;
import com.github.parker8283.bon2.gui.StartListener;
import com.github.parker8283.bon2.srg.Mapping;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class BON2Gui extends JFrame {
    public static final String ERROR_DIALOG_TITLE = "Error - BON2";
    public static final String PREFS_KEY_FORGEVER = "forgeVer";
    public final Preferences prefs = Preferences.userNodeForPackage(BON2Gui.class);
    private static final long serialVersionUID = -619289399889088924L;

    private JPanel contentPane;
    private JTextField inputJarLoc;
    private JLabel lblOutput;
    private JTextField outputJarLoc;
    private JButton btnBrouseOutput;
    private JLabel lblForgeVer;
    private JLabel lblProgressText;
    private JProgressBar masterProgress;

    /**
     * Create the frame.
     */
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

        JButton btnBrouseInput = new JButton("Browse");
        btnBrouseInput.addMouseListener(new BrowseListener(this, true, inputJarLoc));

        lblOutput = new JLabel("Output JAR");
        lblOutput.setHorizontalAlignment(SwingConstants.CENTER);

        outputJarLoc = new JTextField();
        outputJarLoc.setColumns(10);

        btnBrouseOutput = new JButton("Browse");
        btnBrouseOutput.addMouseListener(new BrowseListener(this, false, outputJarLoc));

        lblForgeVer = new JLabel("Forge Version");

        JComboBox<MappingVersion> forgeVersions = new JComboBox<MappingVersion>();

        JButton btnRefreshVers = new JButton("Refresh");
        RefreshListener refresh = new RefreshListener(this, forgeVersions);
        btnRefreshVers.addMouseListener(refresh);
        VersionLookup.INSTANCE.refresh(); // make sure we've queried the json, as this will halt the main thread
        refresh.mouseClicked(null); // update the versions initially
        String forgeVer = prefs.get(PREFS_KEY_FORGEVER, "");
        if(!Strings.isNullOrEmpty(forgeVer) && comboBoxToList(forgeVersions).contains(forgeVer)) {
            forgeVersions.setSelectedItem(forgeVer);
        }

        masterProgress = new JProgressBar();

        lblProgressText = new JLabel("Ready!");

        JButton btnStart = new JButton("Go!");
        btnStart.addMouseListener(new StartListener(this, inputJarLoc, outputJarLoc, forgeVersions, lblProgressText, masterProgress));

        lblProgressText.setHorizontalAlignment(SwingConstants.CENTER);
        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup().addContainerGap().addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING).addComponent(lblProgressText, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE).addComponent(btnStart, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE).addGroup(Alignment.LEADING, gl_contentPane.createSequentialGroup().addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addComponent(lblForgeVer).addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false).addComponent(lblInput, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(lblOutput, GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE))).addPreferredGap(ComponentPlacement.RELATED).addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addComponent(inputJarLoc, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE).addComponent(outputJarLoc, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE).addComponent(forgeVersions, 0, 289, Short.MAX_VALUE)).addPreferredGap(ComponentPlacement.RELATED).addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING, false).addComponent(btnRefreshVers, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(btnBrouseOutput, GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE).addComponent(btnBrouseInput, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))).addComponent(masterProgress, GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)).addContainerGap()));
        gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(gl_contentPane.createSequentialGroup().addContainerGap().addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(inputJarLoc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(btnBrouseInput).addComponent(lblInput)).addPreferredGap(ComponentPlacement.RELATED).addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(lblOutput).addComponent(outputJarLoc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(btnBrouseOutput)).addPreferredGap(ComponentPlacement.RELATED).addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(lblForgeVer).addComponent(forgeVersions, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(btnRefreshVers)).addPreferredGap(ComponentPlacement.RELATED).addComponent(btnStart).addPreferredGap(ComponentPlacement.RELATED).addComponent(masterProgress, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGap(7).addComponent(lblProgressText).addContainerGap(7, Short.MAX_VALUE)));
        contentPane.setLayout(gl_contentPane);
    }

    public JTextField getOutputField() {
        return outputJarLoc;
    }

    private List<String> comboBoxToList(JComboBox comboBox) {
        List<String> ret = Lists.newArrayList();
        for(int i = 0; i < comboBox.getItemCount(); i++) {
            ret.add(comboBox.getItemAt(i).toString());
        }
        return ret;
    }
}
