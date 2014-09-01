package com.github.parker8283.bon2.listener;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;

import javax.swing.*;

public class RefreshListener extends MouseAdapter {
    private JComboBox comboBox;

    public RefreshListener(JComboBox comboBox) {
        this.comboBox = comboBox;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        File userGradleDir = new File(System.getProperty("user.home") + File.separator + ".gradle");
        if(!userGradleDir.exists()) {
            throw new RuntimeException("No user .gradle folder found. You must run ForgeGradle at least once in order to use this tool.", new FileNotFoundException("No user .gradle folder found"));
        } else if(!userGradleDir.isDirectory()) {
            throw new RuntimeException("The user .gradle isn't a folder. Delete it and try again.", new NotDirectoryException(userGradleDir.getAbsolutePath()));
        }

        File forgeVers = new File(userGradleDir, "caches" + File.separator + "minecraft" + File.separator + "net" + File.separator + "minecraftforge" + File.separator + "forge");
        String[] versions = forgeVers.list();
        for(String version : versions) {
            comboBox.addItem(version);
        }
    }
}
