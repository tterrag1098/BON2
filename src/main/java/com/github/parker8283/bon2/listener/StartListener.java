package com.github.parker8283.bon2.listener;

import com.github.parker8283.bon2.data.BONFiles;
import com.github.parker8283.bon2.srg.ClassCollection;
import com.github.parker8283.bon2.srg.Repo;
import com.github.parker8283.bon2.util.JarReader;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class StartListener extends MouseAdapter {
    private File input;
    private File output;
    private String forgeVer;
    private ClassCollection inputCC;

    public StartListener(File input, File output, String forgeVer) {
        this.input = input;
        this.output = output;
        this.forgeVer = forgeVer;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        File srgsFolder = new File(BONFiles.MINECRAFTFORGE_FORGE_FOLDER, forgeVer + File.separator + "srgs");
        try {
            Repo.loadMappings(srgsFolder);
            inputCC = JarReader.readFromJar(input);
        } catch (Exception ex) {
            throw new RuntimeException("There was an error.", ex);
        }
    }

    public ClassCollection getInputClassCollection() {
        return inputCC;
    }
}
