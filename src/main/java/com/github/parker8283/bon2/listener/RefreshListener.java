package com.github.parker8283.bon2.listener;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;

import com.github.parker8283.bon2.data.BONFiles;
import com.google.common.collect.Lists;

public class RefreshListener extends MouseAdapter {
    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+(\\.\\d+)?(_\\w+)?-\\d+\\.\\d+\\.\\d+\\.\\d+(-.+)?");

    private JComboBox comboBox;
    private final Matcher versionMatcher = VERSION_PATTERN.matcher("");

    public RefreshListener(JComboBox comboBox) {
        this.comboBox = comboBox;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(!BONFiles.USER_GRADLE_FOLDER.exists()) {
            throw new RuntimeException("No user .gradle folder found. You must run ForgeGradle at least once in order to use this tool.", new FileNotFoundException("No user .gradle folder found"));
        } else if(!BONFiles.USER_GRADLE_FOLDER.isDirectory()) {
            throw new RuntimeException("The user .gradle isn't a folder. Delete it and try again.", new NotDirectoryException(BONFiles.USER_GRADLE_FOLDER.getAbsolutePath()));
        }

        comboBox.removeAllItems();
        File[] versionFolders = BONFiles.MINECRAFTFORGE_FORGE_FOLDER.listFiles();
        List<File> acceptedVersions = Lists.newArrayList();
        for(File file : versionFolders) {
            String name = file.getName();
            if(!name.startsWith("1.6") && versionMatcher.reset(name).matches()) {
                acceptedVersions.add(file);
            }
        }
        List<String> versions = Lists.newArrayList();
        for(File file : acceptedVersions) {
            if(new File(file, "srgs").exists()) {
                versions.add(file.getName() + "-shipped");
            }
            if(hasAdditionalMappings(file)) {
                List<String> additionalMappings = Lists.newArrayList();
                File mappingDir = new File(file, "snapshot");
                if(mappingDir.exists()) {
                    for(String date : mappingDir.list()) {
                        additionalMappings.add("snapshot_" + date);
                    }
                }
                mappingDir = new File(file, "snapshot_nodoc");
                if(mappingDir.exists()) {
                    for(String date : mappingDir.list()) {
                        additionalMappings.add("snapshot_nodoc_" + date);
                    }
                }
                mappingDir = new File(file, "stable");
                if(mappingDir.exists()) {
                    for(String date : mappingDir.list()) {
                        additionalMappings.add("stable_" + date);
                    }
                }
                mappingDir = new File(file, "stable_nodoc");
                if(mappingDir.exists()) {
                    for(String date : mappingDir.list()) {
                        additionalMappings.add("stable_nodoc_" + date);
                    }
                }
                for(String mapping : additionalMappings) {
                    versions.add(file.getName() + "-" + mapping);
                }
            }
        }
        for(String version : versions) {
            //noinspection unchecked
            comboBox.addItem(version);
        }
    }

    private boolean hasAdditionalMappings(File file) {
        return new File(file, "snapshot").exists() || new File(file, "snapshot_nodoc").exists() || new File(file, "stable").exists() || new File(file, "stable_nodoc").exists();
    }
}
