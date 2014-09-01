package com.github.parker8283.bon2;

import java.io.File;

import javax.swing.*;

import com.github.parker8283.bon2.listener.BrowseListener;
import com.github.parker8283.bon2.listener.RefreshListener;
import com.github.parker8283.bon2.listener.StartListener;

public class BON2 {
    private JPanel contentPane;
    private JTextField inputJarLoc;
    private JTextField outputJarLoc;
    private JLabel lblInput;
    private JLabel lblOutput;
    private JButton btnStart;
    private JButton btnBrouseInput;
    private JButton btnBrouseOutput;
    private JProgressBar masterProgress;
    private JLabel lblForgeVer;
    private JComboBox forgeVersions;
    private JButton btnRefreshVers;

    public BON2() {
        btnBrouseInput.addMouseListener(new BrowseListener(contentPane.getParent(), true, inputJarLoc));
        btnBrouseOutput.addMouseListener(new BrowseListener(contentPane.getParent(), false, outputJarLoc));
        btnRefreshVers.addMouseListener(new RefreshListener(forgeVersions));
        btnStart.addMouseListener(new StartListener(new File(inputJarLoc.getText()), new File(outputJarLoc.getText()), (String)forgeVersions.getSelectedItem()));
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("BON2");
        frame.setResizable(false);
        frame.setContentPane(new BON2().contentPane);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
