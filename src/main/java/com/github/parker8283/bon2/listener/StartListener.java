package com.github.parker8283.bon2.listener;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class StartListener extends MouseAdapter {

    public StartListener(File input, File output, String forgeVer) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        File userGradleFolder = new File(System.getProperty("user.home") + File.separator + ".gradle");
    }
}
