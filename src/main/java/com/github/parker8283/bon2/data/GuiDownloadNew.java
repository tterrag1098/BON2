package com.github.parker8283.bon2.data;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.util.*;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.github.parker8283.bon2.gui.GUIProgressListener;
import com.github.parker8283.bon2.util.BONUtils;
import com.github.parker8283.bon2.util.DownloadUtils;
import com.github.parker8283.bon2.util.MappingVersions;
import com.github.parker8283.bon2.util.MappingVersions.MappingVersion;

import net.minecraftforge.srgutils.MinecraftVersion;

public class GuiDownloadNew extends JFrame {
    private static final String[] MONTHS = new DateFormatSymbols().getMonths();
    private class DownloadMappingsTask implements Runnable {

        private final Queue<MappingVersion> versions;
        private final IProgressListener progress;

        public DownloadMappingsTask(Collection<MappingVersion> urls, IProgressListener progress) {
            this.versions = new LinkedList<>(urls);
            this.progress = progress;
        }

        @Override
        public void run() {
            try {
                progress.start(versions.size(), "Downloading");
                int finished = 0;
                while (!versions.isEmpty()) {
                    MappingVersion entry = versions.poll();
                    //progress.setLabel("Downloading: " + entry.toString());
                    try {
                        if (entry.getType() == MappingVersions.Type.OFFICIAL) {
                            //TODO: Download version manifest, and mapping file, and generate mappings
                        } else {
                            File target = entry.getTarget(BONFiles.FG3_DOWNLOAD_CACHE);
                            if (!DownloadUtils.downloadWithCache(new URL(entry.getUrl()), target, false, false, this.progress)) {
                                JOptionPane.showMessageDialog(GuiDownloadNew.this, "Failed to download " + entry.getUrl(), "Error downloading mappings", JOptionPane.ERROR_MESSAGE);
                                versions.clear();
                                progress.start(0, "Error");
                                progress.setProgress(0);
                            }
                        }
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(GuiDownloadNew.this, e, "Error downloading mappings", JOptionPane.ERROR_MESSAGE);
                        versions.clear();
                        progress.start(0, "Error");
                        progress.setProgress(0);
                        return;
                    }
                    Thread.sleep((finished + 1) % 10 == 0 ? 2000L : 1); // plz no ddos
                    progress.setProgress(++finished);
                }
                progress.setLabel("Done!");
                GuiDownloadNew.this.dispose();
            } catch (InterruptedException e) {
                progress.start(0, "Canceled");
                progress.setProgress(0);
            }
        }
    }

    private final Runnable refresh;

    private Thread downloadTask;

    public GuiDownloadNew(Runnable refresh, final Map<MinecraftVersion, List<MappingVersion>> data) {
        this.refresh = refresh;

        setBounds(100, 100, 540, 500);
        setMinimumSize(new Dimension(540, 370));
        setTitle("Mappings Downloader");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Minecraft Versions");
        final List<MinecraftVersion> sortedVersions = data.keySet().stream().sorted(Collections.reverseOrder()).collect(Collectors.toList());

        for (MinecraftVersion mcver : sortedVersions) {
            DefaultMutableTreeNode mcnode = new DefaultMutableTreeNode(mcver.toString());
            root.add(mcnode);
            Map<MappingVersions.Type, List<MappingVersion>> types = new EnumMap<>(MappingVersions.Type.class);
            for (MappingVersion mapver : data.get(mcver))
                types.computeIfAbsent(mapver.getType(), t -> new ArrayList<>()).add(mapver);

            types.forEach((t,l) -> {
                switch (t) {
                    case STABLE:
                        DefaultMutableTreeNode stables = new DefaultMutableTreeNode("Stable");
                        mcnode.add(stables);
                        l.stream().sorted(Collections.reverseOrder()).forEach(e -> stables.add(new NameableDefaultMutableTreeNode(e.getVersion() + "", e)));
                        break;
                    case SNAPSHOT:
                        DefaultMutableTreeNode snaps = new DefaultMutableTreeNode("Snapshot");
                        mcnode.add(snaps);

                        Map<Integer, Map<Integer, List<MappingVersion>>> versionsUnsorted = l.stream()
                                .collect(Collectors.groupingBy(e -> e.getVersion() / 10000, Collectors.groupingBy(e -> (e.getVersion() / 100) % 100)));

                        Map<Integer, Map<Integer, List<MappingVersion>>> versions = new TreeMap<>(Collections.reverseOrder());
                        versions.putAll(versionsUnsorted);

                        versions.forEach((year, months) -> {
                            Map<Integer, List<MappingVersion>> sorted = new TreeMap<>(Collections.reverseOrder());
                            sorted.putAll(months);

                            DefaultMutableTreeNode yearNode = new DefaultMutableTreeNode(year);
                            sorted.forEach((month, list) -> {
                                DefaultMutableTreeNode monthNode = new DefaultMutableTreeNode(MONTHS[month - 1]);
                                list.stream().sorted(Collections.reverseOrder()).forEach(m -> monthNode.add(new NameableDefaultMutableTreeNode(m.getVersion() + "", m)));
                                yearNode.add(monthNode);
                            });
                            snaps.add(yearNode);
                        });
                        break;
                    case OFFICIAL:
                        mcnode.add(new NameableDefaultMutableTreeNode("Official", l.get(0)));
                        break;
                }
                //node.add(new DefaultMutableTreeNode(mapver, false));
            });
            //types.values().forEach(mcnode::add);
        }

        JTree list = new JTree(root);
        list.setVisibleRowCount(-1);

        JScrollPane listScroller = new JScrollPane(list);

        JButton buttonDownload = new JButton("Download Selected");
        buttonDownload.setEnabled(false);
        list.addTreeSelectionListener(e -> {
            buttonDownload.setEnabled(list.getSelectionCount() > 0);
            if (buttonDownload.isEnabled()) {
                int count = getSelectedRecursive(list).size();
                buttonDownload.setText("Download Selected (" + count + ")");
            } else {
                buttonDownload.setText("Download Selected");
            }
        });

        JButton buttonDownloadLatest = new JButton("Download Latest Snapshot");
        buttonDownloadLatest.setToolTipText("Download the latest snapshot for the most recent version of MC");

        JButton buttonDownloadAllLatest = new JButton("Download All Latest");
        buttonDownloadAllLatest.setToolTipText("Download the latest snapshot and stable for all MC versions");

        JTextField textFieldVersion = new JTextField();
        textFieldVersion.setColumns(10);

        JLabel lblEnterVersion = new JLabel("Enter Version:");
        lblEnterVersion.setHorizontalAlignment(SwingConstants.CENTER);

        JButton buttonDownloadSpecific = new JButton("Download");
        buttonDownloadSpecific.setEnabled(false);
        BONUtils.addChangeListener(textFieldVersion, e -> {
            buttonDownloadSpecific.setEnabled(textFieldVersion.getText().matches("stable_[0-9]{1,3}|snapshot_[0-9]{8}"));
        });

        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.setEnabled(false);
        buttonCancel.addActionListener(e -> {
            downloadTask.interrupt();
            buttonCancel.setEnabled(false);
        });

        JProgressBar progressBar = new JProgressBar();

        JLabel lblProgressText = new JLabel("Select an option above");

        // Set download tasks
        buttonDownload.addActionListener(e -> {
            List<MappingVersion> selected = getSelectedRecursive(list).stream()
                    .map(o -> (MappingVersion)((DefaultMutableTreeNode)o).getUserObject())
                    .collect(Collectors.toList());

            int confirmed = 0;
            if (selected.size() >= 20) {
                confirmed = JOptionPane.showConfirmDialog(GuiDownloadNew.this, "This will download " + selected.size() + " mappings versions. Are you sure?", "Confirm", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            }
            if (confirmed == 0) {
                startDownloadTask(selected, lblProgressText, progressBar, buttonCancel);
            }
        });
        buttonDownloadLatest.addActionListener(evt -> {
           List<MappingVersion> lst = data.get(sortedVersions.get(0));
           startDownloadTask(Collections.singletonList(lst.get(lst.size() - 1)), lblProgressText, progressBar, buttonCancel);
        });
        buttonDownloadAllLatest.addActionListener(evt -> {
            List<MappingVersion> entries = new ArrayList<>();
            for (MinecraftVersion mcver : sortedVersions) {
                EnumSet<MappingVersions.Type> missing = EnumSet.allOf(MappingVersions.Type.class);
                data.get(mcver).stream().sorted(Collections.reverseOrder()).forEach(e -> {
                    MappingVersions.Type type = e.getType();
                    if (missing.contains(type)) {
                        entries.add(e);
                        missing.remove(type);
                    }
                });
            }
            startDownloadTask(entries, lblProgressText, progressBar, buttonCancel);
        });
        buttonDownloadSpecific.addActionListener(evt -> {
            MappingVersion ver = MappingVersions.getFromString(textFieldVersion.getText());
            if (ver != null) {
                startDownloadTask(Collections.singletonList(ver), lblProgressText, progressBar, buttonCancel);
            } else {
                lblProgressText.setText("Invalid mappings version");
            }
        });


        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout.setHorizontalGroup(
            groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(listScroller, GroupLayout.PREFERRED_SIZE, 294, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.UNRELATED)
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addComponent(buttonDownload, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 160, 200)
                        .addComponent(progressBar, GroupLayout.DEFAULT_SIZE, 160, 200)
                        .addComponent(lblProgressText, GroupLayout.DEFAULT_SIZE, 160, 200)
                        .addComponent(buttonCancel, GroupLayout.DEFAULT_SIZE, 160, 200)
                        .addComponent(buttonDownloadLatest, GroupLayout.DEFAULT_SIZE, 160, 200)
                        .addComponent(buttonDownloadAllLatest, GroupLayout.DEFAULT_SIZE, 160, 200)
                        .addComponent(textFieldVersion, GroupLayout.DEFAULT_SIZE, 160, 200)
                        .addComponent(lblEnterVersion, GroupLayout.DEFAULT_SIZE, 160, 200)
                        .addComponent(buttonDownloadSpecific, GroupLayout.DEFAULT_SIZE, 160, 200)
                    )
                    .addContainerGap()
                )
        );
        groupLayout.setVerticalGroup(
            groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addComponent(listScroller, GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                        .addGroup(groupLayout.createSequentialGroup()
                            .addComponent(buttonDownload)
                            .addGap(4)
                            .addComponent(buttonDownloadLatest)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(buttonDownloadAllLatest)
                            .addGap(38)
                            .addComponent(lblEnterVersion)
                            .addGap(5)
                            .addComponent(textFieldVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(buttonDownloadSpecific)
                            .addPreferredGap(ComponentPlacement.RELATED, 192, Short.MAX_VALUE)
                            .addComponent(lblProgressText)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(buttonCancel)
                        )
                    )
                    .addContainerGap()
                )
        );
        getContentPane().setLayout(groupLayout);
    }

    @Override
    public void dispose() {
        super.dispose();
        this.refresh.run();
    }

    private void startDownloadTask(Collection<MappingVersion> entries, JLabel lbl, JProgressBar prog, JButton cancel) {
        cancel.setEnabled(true);
        downloadTask = new Thread(new DownloadMappingsTask(entries, new GUIProgressListener(lbl, prog)), "Mappings Downloader");
        downloadTask.start();
    }

    private static List<Object> getSelectedRecursive(JTree tree) {
        return Arrays.stream(tree.getSelectionPaths())
                .map(path -> getChildren(tree.getModel(), path))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private static List<Object> getChildren(TreeModel tree, TreePath path) {
        Object parent = path.getLastPathComponent();
        int childCount = tree.getChildCount(parent);
        if (childCount == 0) {
            return Collections.singletonList(parent);
        }
        List<Object> children = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            children.addAll(getChildren(tree, path.pathByAddingChild(tree.getChild(parent, i))));
        }
        return children;
    }

    private static final long serialVersionUID = -5671034374840427145L;

    private static class NameableDefaultMutableTreeNode extends DefaultMutableTreeNode {
        private static final long serialVersionUID = -7895415303737908716L;
        private final String name;
        public NameableDefaultMutableTreeNode(String name, Object obj) {
            super(obj, false);
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
