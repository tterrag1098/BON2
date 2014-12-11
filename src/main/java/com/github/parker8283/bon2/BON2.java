package com.github.parker8283.bon2;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

import com.github.parker8283.bon2.data.BONFiles;
import com.github.parker8283.bon2.listener.BrowseListener;
import com.github.parker8283.bon2.listener.DropListener;
import com.github.parker8283.bon2.listener.RefreshListener;
import com.github.parker8283.bon2.listener.StartListener;

public class BON2 extends JFrame {
    public static final File ASM_4_JAR = new File(BONFiles.MODULES_FILES_FOLDER, "org.ow2.asm" + File.separator + "asm-debug-all" + File.separator + "4.1" + File.separator + "dd6ba5c392d4102458494e29f54f70ac534ec2a2" + File.separator + "asm-debug-all-4.1.jar");
    public static final File ASM_5_JAR = new File(BONFiles.MODULES_FILES_FOLDER, "org.ow2.asm" + File.separator + "asm-debug-all" + File.separator + "5.0.3" + File.separator + "f9e364ae2a66ce2a543012a4668856e84e5dab74" + File.separator + "asm-debug-all-5.0.3.jar");
    public static final File GUAVA_JAR = new File(BONFiles.MODULES_FILES_FOLDER, "com.google.guava" + File.separator + "guava" + File.separator + "17.0" + File.separator + "9c6ef172e8de35fd8d4d8783e4821e57cdef7445" + File.separator + "guava-17.0.jar");

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
        addASMToClasspath();
        addGuavaToClasspath();
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
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(100, 100, 500, 230);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        JLabel lblInput = new JLabel("Input JAR");
        lblInput.setHorizontalAlignment(SwingConstants.CENTER);

        inputJarLoc = new JTextField();
        inputJarLoc.setDropTarget(new DropListener(inputJarLoc));
        inputJarLoc.setColumns(10);

        JButton btnBrouseInput = new JButton("Browse");
        btnBrouseInput.addMouseListener(new BrowseListener(this, true, inputJarLoc));

        lblOutput = new JLabel("Output JAR");
        lblOutput.setHorizontalAlignment(SwingConstants.CENTER);

        outputJarLoc = new JTextField();
        outputJarLoc.setDropTarget(new DropListener(outputJarLoc));
        outputJarLoc.setColumns(10);

        btnBrouseOutput = new JButton("Browse");
        btnBrouseOutput.addMouseListener(new BrowseListener(this, false, outputJarLoc));

        lblForgeVer = new JLabel("Forge Version");

        JComboBox forgeVersions = new JComboBox();

        JButton btnRefreshVers = new JButton("Refresh");
        RefreshListener refreshListener = new RefreshListener(forgeVersions);
        refreshListener.reloadForgeVersions();
        btnRefreshVers.addMouseListener(refreshListener);

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

    private static void addASMToClasspath() {
        try {
            Class.forName("org.objectweb.asm.Opcodes");
        } catch(ClassNotFoundException e) {
            System.out.println("ASM isn't already in classpath. Adding it...");
            if(!ASM_5_JAR.exists()) {
                System.err.println("ASM 5 could not be found. Trying ASM 4...");
                if(!ASM_4_JAR.exists()) {
                    throw new RuntimeException("ASM couldn't be found. You must run setupDevWorkspace or setupDecompWorkspace at least once in order to use this.", new FileNotFoundException("asm-debug-all-5.0.3.jar nor asm-debug-all-4.1.jar could be found"));
                }
                try {
                    addUrl(ASM_4_JAR.toURI().toURL());
                    Class.forName("org.objectweb.asm.Opcodes");
                } catch(MalformedURLException ex) {
                    ex.printStackTrace();
                } catch(ClassNotFoundException e1) {
                    throw new RuntimeException("ASM couldn't be added to the classpath. Please report to Parker8283.", e1);
                }
            }
            try {
                addUrl(ASM_5_JAR.toURI().toURL());
                Class.forName("org.objectweb.asm.Opcodes");
            } catch(MalformedURLException ex) {
                ex.printStackTrace();
            } catch(ClassNotFoundException e1) {
                throw new RuntimeException("ASM couldn't be added to the classpath. Please report to Parker8283.", e1);
            }
        }
    }

    private static void addGuavaToClasspath() {
        try {
            Class.forName("com.google.common.collect.Maps");
        } catch(ClassNotFoundException e) {
            System.out.println("Guava isn't already in classpath. Adding it...");
            if(!GUAVA_JAR.exists()) {
                throw new RuntimeException("Guava couldn't be found. You must run setupDevWorkspace or setupDecompWorkspace at least once in order to use this.", new FileNotFoundException("guava-17.0.jar could not be found"));
            }
            try {
                addUrl(GUAVA_JAR.toURI().toURL());
                Class.forName("com.google.common.collect.Maps");
            } catch(MalformedURLException ex) {
                ex.printStackTrace();
            } catch(ClassNotFoundException e1) {
                throw new RuntimeException("Guava couldn't be added to the classpath. Please report to Parker8283.", e1);
            }
        }
    }

    private static void addUrl(URL url) {
        URLClassLoader sysloader = (URLClassLoader)BON2.class.getClassLoader();
        Class sysclass = URLClassLoader.class;
        try {
            //noinspection unchecked
            Method method = sysclass.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(sysloader, url);
        } catch(Exception e) {
            throw new RuntimeException("Could not add ASM to classpath. Will not continue.", e);
        }
    }
}
