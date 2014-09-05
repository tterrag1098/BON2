package com.github.parker8283.bon2.listener;

import com.github.parker8283.bon2.data.BONFiles;
import com.github.parker8283.bon2.srg.ClassCollection;
import com.github.parker8283.bon2.srg.Repo;
import com.github.parker8283.bon2.util.JarUtils;
import com.github.parker8283.bon2.util.Remapper;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.*;

public class StartListener extends MouseAdapter {
    private JTextField input;
    private JTextField output;
    private JComboBox forgeVer;
    private ClassCollection inputCC;
    private ClassCollection outputCC;

    public StartListener(JTextField input, JTextField output, JComboBox forgeVer) {
        this.input = input;
        this.output = output;
        this.forgeVer = forgeVer;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        File srgsFolder = new File(BONFiles.MINECRAFTFORGE_FORGE_FOLDER, forgeVer.getSelectedItem().toString() + File.separator + "srgs");
        try {
            Repo.loadMappings(srgsFolder);
            inputCC = JarUtils.readFromJar(new File(input.getText()));
            outputCC = Remapper.remap(inputCC);
            JarUtils.writeToJar(outputCC, new File(output.getText()));
        } catch (Exception ex) {
            throw new RuntimeException("There was an error.", ex);
        }
    }

    public ClassCollection getInputClassCollection() {
        return inputCC;
    }

    public ClassCollection getOutputClassCollection() {
        return outputCC;
    }
}
