package com.github.parker8283.bon2.listener;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;

import com.github.parker8283.bon2.data.BONFiles;

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
        String[] versions = BONFiles.MINECRAFTFORGE_FORGE_FOLDER.list();
        for(String version : versions) {
            if(!version.startsWith("1.6") && versionMatcher.reset(version).matches()) {
                //noinspection unchecked
                comboBox.addItem(version);
            }
        }
    }
}
