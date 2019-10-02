package com.github.parker8283.bon2.data;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Files;
import java.text.DateFormatSymbols;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.github.parker8283.bon2.data.VersionJson.MappingsJson;
import com.github.parker8283.bon2.gui.GUIProgressListener;
import com.github.parker8283.bon2.gui.RefreshListener;
import com.github.parker8283.bon2.util.BONUtils;

public class GuiDownloadNew extends JFrame {

    private static final String MAPPINGS_URL_SNAPSHOT = "http://export.mcpbot.bspk.rs/mcp_snapshot/%1$s-%2$s/mcp_snapshot-%1$s-%2$s.zip";
    private static final String MAPPINGS_URL_STABLE = "http://export.mcpbot.bspk.rs/mcp_stable/%1$s-%2$s/mcp_stable-%1$s-%2$s.zip";

    private static class MappingListEntry {
        public final boolean stable;
        public final int version;
        public final String url;

        public MappingListEntry(boolean stable, int version, String url) {
            this.stable = stable;
            this.version = version;
            this.url = url;
        }

        @Override
        public String toString() {
            return (stable ? "stable" : "snapshot") + "_" + version;
        }
    }

    private static class ComparableVersion implements Comparable<ComparableVersion> {

        private final String version;
        private final int[] vdata;

        public ComparableVersion(String version) {
            this.version = version;
            this.vdata = Arrays.stream(version.split("\\.")).mapToInt(Integer::parseInt).toArray();
        }

        public String version() {
            return version;
        }

        @Override
        public String toString() {
            return version();
        }

        @Override
        public int compareTo(ComparableVersion o) {
            int idx = 0;
            while (idx < vdata.length) {
                if (idx >= o.vdata.length) {
                    return -1;
                }
                int comp = Integer.compare(o.vdata[idx], vdata[idx]);
                if (comp == 0) {
                    idx++;
                } else {
                    return comp;
                }
            }
            return 0;
        }
    }

    private class DownloadMappingsTask implements Runnable {

        private final Queue<MappingListEntry> urls;
        private final IProgressListener progress;

        public DownloadMappingsTask(Collection<MappingListEntry> urls, IProgressListener progress) {
            this.urls = new LinkedList<>(urls);
            this.progress = progress;
        }

        @Override
        public void run() {
            progress.start(urls.size(), "Downloading");
            int finished = 0;
            while (!urls.isEmpty()) {
                MappingListEntry entry = urls.poll();
                progress.setLabel("Downloading: " + entry.toString());
                try (InputStream in = new URL(entry.url).openStream()) {
                    File rootFolder = BONFiles.OCEANLABS_MCP_FOLDER;
                    File temp = rootFolder.toPath()
                            .resolve(entry.stable ? "mcp_stable" : "mcp_snapshot")
                            .resolve(Integer.toString(entry.version))
                            .resolve("temp.zip")
                            .toFile();

                    File folder = temp.getParentFile();
                    if (folder.exists()) {
                        // Wipe old CSVs
                        for (File file : folder.listFiles((dir, name) -> name.endsWith(".csv"))) {
                            file.delete();
                        }
                    } else {
                        folder.mkdirs();
                    }

                    if (temp.exists()) {
                        System.err.println("Temporary .zip already exists, deleting...");
                        temp.delete();
                    }
                    Files.copy(in, temp.toPath());
                    Thread.sleep(1);
                    BONUtils.extractZip(temp);
                    temp.delete();
                    Thread.sleep((finished + 1) % 10 == 0 ? 2000L : 1); // plz no ddos
                } catch (InterruptedException | ClosedByInterruptException e) {
                    progress.start(0, "Canceled");
                    progress.setProgress(0);
                    return;
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(GuiDownloadNew.this, e, "Error downloading mappings", JOptionPane.ERROR_MESSAGE);
                    urls.clear();
                    progress.start(0, "Error");
                    progress.setProgress(0);
                    return;
                }
                progress.setProgress(++finished);
            }
            progress.setLabel("Done!");
            GuiDownloadNew.this.dispose();
        }
    }

    private final RefreshListener refresh;

    private Thread downloadTask;

    public GuiDownloadNew(RefreshListener refresh) throws IOException {

        this.refresh = refresh;

        setBounds(100, 100, 540, 500);
        setMinimumSize(new Dimension(540, 370));
        setTitle("Mappings Downloader");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        VersionLookup.INSTANCE.refresh();

        VersionJson data = VersionLookup.INSTANCE.getVersions();
        if (data == null || data.getVersions().isEmpty()) {
            throw new IOException("Version list empty/missing!");
        }

        TreeMap<ComparableVersion, MappingsJson> mappings = new TreeMap<>();
        for (String v : data.getVersions()) {
            mappings.put(new ComparableVersion(v), data.getMappings(v));
        }

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Minecraft Versions");

        for (Entry<ComparableVersion, MappingsJson> e : mappings.entrySet()) {
            DefaultMutableTreeNode mcver = new DefaultMutableTreeNode(e.getKey());
            root.add(mcver);
            if (e.getValue().getStables().length > 0) {
                DefaultMutableTreeNode stables = new DefaultMutableTreeNode("Stable");
                mcver.add(stables);
                int[] versions = e.getValue().getStables();
                Arrays.sort(versions);
                for (int i = versions.length - 1; i >= 0; i--) {
                    int stable = versions[i];
                    stables.add(new DefaultMutableTreeNode(new MappingListEntry(true, stable, String.format(MAPPINGS_URL_STABLE, stable, e.getKey())), false));
                }
            }
            DefaultMutableTreeNode snaps = new DefaultMutableTreeNode("Snapshot");
            mcver.add(snaps);
            int[] snapids = e.getValue().getSnapshots();

            Map<Integer, Map<Integer, List<String>>> versionsUnsorted = Arrays.stream(snapids)
                    .mapToObj(Integer::toString)
                    .collect(Collectors.groupingBy(s -> Integer.parseInt(s.substring(0, 4)), Collectors.groupingBy(s -> Integer.parseInt(s.substring(4, 6)))));

            Comparator<Integer> reverseCompare = (i1, i2) -> Integer.compare(i2, i1);
            Map<Integer, Map<Integer, List<String>>> versions = new TreeMap<>(reverseCompare);
            versions.putAll(versionsUnsorted);

            for (Entry<Integer, Map<Integer, List<String>>> byYear : versions.entrySet()) {
                Map<Integer, List<String>> sorted = new TreeMap<>(reverseCompare);
                sorted.putAll(byYear.getValue());
                DefaultMutableTreeNode year = new DefaultMutableTreeNode(byYear.getKey());
                for (Entry<Integer, List<String>> byMonth : sorted.entrySet()) {
                    DefaultMutableTreeNode month = new DefaultMutableTreeNode(new DateFormatSymbols().getMonths()[byMonth.getKey() - 1]);
                    for (String v : byMonth.getValue()) {
                        month.add(new DefaultMutableTreeNode(new MappingListEntry(false, Integer.parseInt(v), String.format(MAPPINGS_URL_SNAPSHOT, v, e.getKey())), false));
                    }
                    year.add(month);
                }
                snaps.add(year);
            }
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
            List<MappingListEntry> selected = getSelectedRecursive(list).stream()
                    .map(o -> (MappingListEntry) ((DefaultMutableTreeNode)o).getUserObject())
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
           Entry<ComparableVersion, MappingsJson> e = mappings.firstEntry();
           startDownloadTask(Collections.singletonList(jsonToEntry(false, e.getValue(), e.getKey().version, OptionalInt.empty())), lblProgressText, progressBar, buttonCancel);
        });
        buttonDownloadAllLatest.addActionListener(evt -> {
            List<MappingListEntry> entries = new ArrayList<>();

            // Do all stables, then all snapshots
            for (Entry<ComparableVersion, MappingsJson> e : mappings.entrySet()) {
                entries.add(jsonToEntry(true,  e.getValue(), e.getKey().version, OptionalInt.empty()));
            }
            for (Entry<ComparableVersion, MappingsJson> e : mappings.entrySet()) {
                entries.add(jsonToEntry(false, e.getValue(), e.getKey().version, OptionalInt.empty()));
            }

            entries.remove(null);
            startDownloadTask(entries, lblProgressText, progressBar, buttonCancel);
        });
        buttonDownloadSpecific.addActionListener(evt -> {
            String text = textFieldVersion.getText();
            boolean stable = text.startsWith("stable");
            int version = Integer.parseInt(text.substring(stable ? 7 : 9));
            MappingListEntry entry = null;
            for (Entry<ComparableVersion, MappingsJson> e : mappings.entrySet()) {
                entry = jsonToEntry(stable, e.getValue(), e.getKey().version, OptionalInt.of(version));
                if (entry != null) break;
            }
            if (entry != null) {
                startDownloadTask(Collections.singletonList(entry), lblProgressText, progressBar, buttonCancel);
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
                        .addComponent(buttonDownloadSpecific, GroupLayout.DEFAULT_SIZE, 160, 200))
                    .addContainerGap())
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
                            .addComponent(buttonCancel)))
                    .addContainerGap())
        );
        getContentPane().setLayout(groupLayout);
    }

    @Override
    public void dispose() {
        super.dispose();
        this.refresh.mouseClicked(null);
    }

    private void startDownloadTask(Collection<MappingListEntry> entries, JLabel lbl, JProgressBar prog, JButton cancel) {
        cancel.setEnabled(true);
        downloadTask = new Thread(new DownloadMappingsTask(entries, new GUIProgressListener(lbl, prog)), "Mappings Downloader");
        downloadTask.start();
    }

    /**
     * @param version If absent, returns the latest.
     */
    private MappingListEntry jsonToEntry(boolean stable, MappingsJson json, String mcver, OptionalInt version) {
        int[] allversions = stable ? json.getStables() : json.getSnapshots();
        if (allversions.length > 0) {
            int v = version.orElse(allversions[0]);
            if (arrayContains(allversions, v)) {
                return new MappingListEntry(stable, v, String.format(stable ? MAPPINGS_URL_STABLE : MAPPINGS_URL_SNAPSHOT, Integer.toString(v), mcver));
            }
        }
        return null;
    }

    private static boolean arrayContains(final int[] array, final int value) {
        for (int n : array)
            if (n == value)
                return true;
        return false;
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
}
