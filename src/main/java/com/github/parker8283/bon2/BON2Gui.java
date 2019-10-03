package com.github.parker8283.bon2;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

import com.github.parker8283.bon2.data.BONFiles;
import com.github.parker8283.bon2.data.GuiDownloadNew;
import com.github.parker8283.bon2.data.MappingVersion;
import com.github.parker8283.bon2.data.VersionLookup;
import com.github.parker8283.bon2.gui.BrowseListener;
import com.github.parker8283.bon2.gui.GUIProgressListener;
import com.github.parker8283.bon2.gui.JarDropTarget;
import com.github.parker8283.bon2.gui.LinuxBrowseListener;
import com.github.parker8283.bon2.gui.RefreshListener;
import com.github.parker8283.bon2.gui.SimpleRefreshListener;
import com.github.parker8283.bon2.gui.StartListener;
import com.github.parker8283.bon2.util.DownloadUtils;
import com.github.parker8283.bon2.util.MCPVersions;
import com.github.parker8283.bon2.util.MCPVersions.MCPVersion;
import com.github.parker8283.bon2.util.MinecraftVersions;
import com.github.parker8283.bon2.util.OSUtils;

import net.minecraftforge.srgutils.MinecraftVersion;

public class BON2Gui extends JFrame {

    public static final String ERROR_DIALOG_TITLE = "Error - BON2";
    private static final String PREFS_KEY_MC_VERSION = "mcVer";
    public static final String PREFS_KEY_VERSION_MAP = "forgeVer";
    public static final String PREFS_KEY_OPEN_LOC = "openLoc";
    public static final String PREFS_KEY_SAVE_LOC = "closeLoc";

    private static final long serialVersionUID = -619289399889088924L;

    public final Preferences prefs = Preferences.userNodeForPackage(BON2Gui.class);

    private JPanel contentPane;
    private JTextField inputJarLoc;
    private JLabel lblOutput;
    private JTextField outputJarLoc;
    private JButton btnBrouseOutput;
    private JLabel lblMinecraftVer;
    private JLabel lblMappingsVer;
    private JLabel lblProgressText;
    private JProgressBar masterProgress;

    public BON2Gui() {
        setMinimumSize(new Dimension(550, 240));
        setTitle("BON2");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(100, 100, 550, 240);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        JLabel lblInput = new JLabel("Input JAR");
        lblInput.setHorizontalAlignment(SwingConstants.CENTER);

        inputJarLoc = new JTextField();
        inputJarLoc.setColumns(10);
        inputJarLoc.setDropTarget(new JarDropTarget(this, path -> {
            inputJarLoc.setText(path);
            outputJarLoc.setText(path.replace(".jar", "-deobf.jar"));
        }));

        JButton btnBrouseInput = new JButton("Browse");

        if (OSUtils.getOS() != OSUtils.OS.Linux) {
            btnBrouseInput.addMouseListener(new BrowseListener(this, true, inputJarLoc));
        } else {
            btnBrouseInput.addMouseListener(new LinuxBrowseListener(this, true, inputJarLoc));
        }

        lblOutput = new JLabel("Output JAR");
        lblOutput.setHorizontalAlignment(SwingConstants.CENTER);

        outputJarLoc = new JTextField();
        outputJarLoc.setColumns(10);
        outputJarLoc.setDropTarget(new JarDropTarget(this, path -> outputJarLoc.setText(path)));

        btnBrouseOutput = new JButton("Browse");

        if (OSUtils.getOS() != OSUtils.OS.Linux) {
            btnBrouseOutput.addMouseListener(new BrowseListener(this, false, outputJarLoc));
        } else {
            btnBrouseOutput.addMouseListener(new LinuxBrowseListener(this, false, outputJarLoc));
        }

        // Minecraft Versions
        lblMinecraftVer = new JLabel("Minecraft");
        lblMinecraftVer.setHorizontalAlignment(SwingConstants.CENTER);
        JComboBox<MinecraftVersion> cmbMinecraftVers = new JComboBox<>();
        JButton btnMinecraftVerRefresh = new JButton("Refresh");
        JButton btnMinecraftVerDownload = new JButton("Download");
        Runnable mcVerRefreshCallback = () -> {
            Object selected = cmbMinecraftVers.getSelectedItem();
            cmbMinecraftVers.removeAllItems();
            MinecraftVersions.getKnownVersions(true).forEach(v -> cmbMinecraftVers.insertItemAt(v, 0));
            cmbMinecraftVers.setSelectedItem(selected);
        };
        mcVerRefreshCallback.run();
        if (cmbMinecraftVers.getSelectedItem() == null)
            cmbMinecraftVers.setSelectedIndex(0);
        btnMinecraftVerRefresh.addMouseListener(new SimpleRefreshListener(mcVerRefreshCallback));

        if (!comboBoxSelect(cmbMinecraftVers, prefs.get(PREFS_KEY_MC_VERSION, "")))
            cmbMinecraftVers.setSelectedIndex(0);

        MCPVersion mcpver = MCPVersions.get((MinecraftVersion)cmbMinecraftVers.getSelectedItem());
        btnMinecraftVerDownload.setEnabled((mcpver != null && !mcpver.getTarget(BONFiles.FG3_DOWNLOAD_CACHE).exists()));

        cmbMinecraftVers.addActionListener(e -> {
            MinecraftVersion selected = (MinecraftVersion)cmbMinecraftVers.getSelectedItem();
            if (selected == null) {
                prefs.remove(PREFS_KEY_MC_VERSION);
                btnMinecraftVerDownload.setEnabled(false);
            } else {
                prefs.put(PREFS_KEY_MC_VERSION, selected.toString());
                MCPVersion v = MCPVersions.get(selected);
                btnMinecraftVerDownload.setEnabled((v != null && !v.getTarget(BONFiles.FG3_DOWNLOAD_CACHE).exists()));
            }
        });
        btnMinecraftVerDownload.addActionListener(e -> {
            btnMinecraftVerDownload.setEnabled(false);
            MinecraftVersion selected = (MinecraftVersion)cmbMinecraftVers.getSelectedItem();
            MCPVersion v = MCPVersions.get(selected);
            if (v == null) {
                lblProgressText.setText("No Minecraft version selected");
                return;
            }
            File target = v.getTarget(BONFiles.FG3_DOWNLOAD_CACHE);
            if (target.exists()) {
                lblProgressText.setText("File already exists! " + target.getAbsolutePath());
                return;
            }

            new Thread(() -> {
                URL url = null;
                try {
                    url = new URL(v.getUrl());
                    if (!DownloadUtils.downloadWithCache(url, target, false, false, new GUIProgressListener(lblProgressText, masterProgress))) {
                        if (target.exists())
                            target.delete();
                        SwingUtilities.invokeLater(() -> {
                            lblProgressText.setText("Download Failed");
                            btnMinecraftVerDownload.setEnabled(true);
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> lblProgressText.setText("Download Complete"));
                    }
                } catch (IOException ex) {
                    System.out.println("Failed to download: " + url + " to " + target.getAbsolutePath());
                    ex.printStackTrace();
                    if (target.exists())
                        target.delete();
                    SwingUtilities.invokeLater(() -> {
                        lblProgressText.setText("Failed to download: " + ex.getMessage());
                        btnMinecraftVerDownload.setEnabled(true);
                    });
                }
            }).start();
        });

        //Mapping Versions
        lblMappingsVer = new JLabel("Mappings");
        lblMappingsVer.setHorizontalAlignment(SwingConstants.CENTER);
        JComboBox<MappingVersion> cmbMappingVersions = new JComboBox<>();

        JButton btnMappingVerRefresh = new JButton("Refresh");
        RefreshListener refresh = new RefreshListener(this, cmbMappingVersions);
        btnMappingVerRefresh.addMouseListener(refresh);
        try {
            VersionLookup.INSTANCE.refresh(); // make sure we've queried the json, as this will halt the main thread
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not load MCP versions from web, mapping versions may be incomplete.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
        refresh.mouseClicked(null);
        if (!comboBoxSelect(cmbMappingVersions, prefs.get(PREFS_KEY_VERSION_MAP, "")))
            cmbMappingVersions.setSelectedIndex(0);

        // Add this after previously saved value is set
        cmbMappingVersions.addActionListener(e -> {
            Object selected = cmbMappingVersions.getSelectedItem();
            if (selected == null) {
                prefs.remove(BON2Gui.PREFS_KEY_VERSION_MAP);
            } else {
                prefs.put(BON2Gui.PREFS_KEY_VERSION_MAP, selected.toString());
            }
        });

        JButton btnMappingVerDownload = new JButton("Download");
        btnMappingVerDownload.addActionListener(e -> {
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
        btnStart.addMouseListener(new StartListener(this, inputJarLoc, outputJarLoc, cmbMappingVersions, lblProgressText, masterProgress));

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
                                .addComponent(lblMappingsVer, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblMinecraftVer, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblInput, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblOutput, GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)
                            )
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                                .addComponent(inputJarLoc, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
                                .addComponent(outputJarLoc, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
                                .addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
                                    .addComponent(cmbMinecraftVers, 0, 255, Short.MAX_VALUE)
                                    .addPreferredGap(ComponentPlacement.RELATED)
                                    .addComponent(btnMinecraftVerDownload, GroupLayout.PREFERRED_SIZE, 91, GroupLayout.PREFERRED_SIZE)
                                )
                                .addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
                                    .addComponent(cmbMappingVersions, 0, 225, Short.MAX_VALUE)
                                    .addPreferredGap(ComponentPlacement.RELATED)
                                    .addComponent(btnMappingVerDownload, GroupLayout.PREFERRED_SIZE, 91, GroupLayout.PREFERRED_SIZE)
                                )
                            )
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING, false)
                                .addComponent(btnMappingVerRefresh, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnMinecraftVerRefresh, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnBrouseOutput, GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
                                .addComponent(btnBrouseInput, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            )
                        )
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
                        .addComponent(lblInput)
                    )
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
                        .addComponent(lblOutput)
                        .addComponent(outputJarLoc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnBrouseOutput)
                    )
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
                        .addComponent(lblMinecraftVer)
                        .addComponent(btnMinecraftVerRefresh)
                        .addComponent(btnMinecraftVerDownload)
                        .addComponent(cmbMinecraftVers, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    )
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
                        .addComponent(lblMappingsVer)
                        .addComponent(btnMappingVerRefresh)
                        .addComponent(btnMappingVerDownload)
                        .addComponent(cmbMappingVersions, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    )
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

    @SuppressWarnings("unused")
    private <T> List<T> comboBoxToList(JComboBox<T> comboBox) {
        List<T> ret = new ArrayList<>();
        for(int i = 0; i < comboBox.getItemCount(); i++) {
            ret.add(comboBox.getItemAt(i));
        }
        return ret;
    }

    private <T> boolean comboBoxSelect(JComboBox<T> comboBox, String value) {
        for(int i = 0; i < comboBox.getItemCount(); i++) {
            T t = comboBox.getItemAt(i);
            if (t != null && value.equals(t.toString())) {
                comboBox.setSelectedIndex(i);
                return true;
            }
        }
        return false;
    }
}
