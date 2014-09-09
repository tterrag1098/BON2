package com.github.parker8283.bon2;

import java.awt.*;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

import com.github.parker8283.bon2.listener.BrowseListener;
import com.github.parker8283.bon2.listener.RefreshListener;
import com.github.parker8283.bon2.listener.StartListener;

public class BON2 extends JFrame {

    private static final long serialVersionUID = -619289399889088924L;
    private JPanel contentPane;
    private JTextField inputJarLoc;
    private JLabel lblOutput;
    private JTextField outputJarLoc;
    private JButton btnBrouseOutput;
    private JLabel lblForgeVer;
    private JLabel lblProgressText;
    private JProgressBar masterProgress;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    BON2 frame = new BON2();
                    frame.setVisible(true);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public BON2() {
        setResizable(false);
        setTitle("BON2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 500, 230);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        JLabel lblInput = new JLabel("Input JAR");
        lblInput.setHorizontalAlignment(SwingConstants.CENTER);

        inputJarLoc = new JTextField();
        inputJarLoc.setColumns(10);

        JButton btnBrouseInput = new JButton("Browse");
        btnBrouseInput.addMouseListener(new BrowseListener(this, true, inputJarLoc));

        lblOutput = new JLabel("Output JAR");
        lblOutput.setHorizontalAlignment(SwingConstants.CENTER);

        outputJarLoc = new JTextField();
        outputJarLoc.setColumns(10);

        btnBrouseOutput = new JButton("Browse");
        btnBrouseOutput.addMouseListener(new BrowseListener(this, false, outputJarLoc));

        lblForgeVer = new JLabel("Forge Version");

        JComboBox forgeVersions = new JComboBox();

        JButton btnRefreshVers = new JButton("Refresh");
        btnRefreshVers.addMouseListener(new RefreshListener(forgeVersions));

        masterProgress = new JProgressBar();

        lblProgressText = new JLabel("Ready!");

        JButton btnStart = new JButton("Go!");
        btnStart.addMouseListener(new StartListener(inputJarLoc, outputJarLoc, forgeVersions, lblProgressText, masterProgress));

        lblProgressText.setHorizontalAlignment(SwingConstants.CENTER);
        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup().addContainerGap().addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING).addComponent(lblProgressText, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE).addComponent(btnStart, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE).addGroup(Alignment.LEADING, gl_contentPane.createSequentialGroup().addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addComponent(lblForgeVer).addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false).addComponent(lblInput, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(lblOutput, GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE))).addPreferredGap(ComponentPlacement.RELATED).addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addComponent(inputJarLoc, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE).addComponent(outputJarLoc, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE).addComponent(forgeVersions, 0, 289, Short.MAX_VALUE)).addPreferredGap(ComponentPlacement.RELATED).addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING, false).addComponent(btnRefreshVers, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(btnBrouseOutput, GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE).addComponent(btnBrouseInput, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))).addComponent(masterProgress, GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)).addContainerGap()));
        gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(gl_contentPane.createSequentialGroup().addContainerGap().addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(inputJarLoc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(btnBrouseInput).addComponent(lblInput)).addPreferredGap(ComponentPlacement.RELATED).addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(lblOutput).addComponent(outputJarLoc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(btnBrouseOutput)).addPreferredGap(ComponentPlacement.RELATED).addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(lblForgeVer).addComponent(forgeVersions, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(btnRefreshVers)).addPreferredGap(ComponentPlacement.RELATED).addComponent(btnStart).addPreferredGap(ComponentPlacement.RELATED).addComponent(masterProgress, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGap(7).addComponent(lblProgressText).addContainerGap(7, Short.MAX_VALUE)));
        contentPane.setLayout(gl_contentPane);
    }
}
