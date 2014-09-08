package com.github.parker8283.bon2;

import com.github.parker8283.bon2.data.BONFiles;
import com.github.parker8283.bon2.listener.BrowseListener;
import com.github.parker8283.bon2.listener.RefreshListener;
import com.github.parker8283.bon2.listener.StartListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import javax.swing.*;

public class BON2 {
    public static final File ASM_JAR = new File(BONFiles.MODULES_FILES_FOLDER, "org.ow2.asm" + File.separator + "asm-debug-all" + File.separator + "4.1" + File.separator + "dd6ba5c392d4102458494e29f54f70ac534ec2a2" + File.separator + "asm-debug-all-4.1.jar");
    public static final File GUAVA_JAR = new File(BONFiles.MODULES_FILES_FOLDER, "com.google.guava" + File.separator + "guava" + File.separator + "17.0" + File.separator + "9c6ef172e8de35fd8d4d8783e4821e57cdef7445" + File.separator + "guava-17.0.jar");
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
    private JLabel lblProgressText;

    public BON2() {
        btnBrouseInput.addMouseListener(new BrowseListener(contentPane.getParent(), true, inputJarLoc));
        btnBrouseOutput.addMouseListener(new BrowseListener(contentPane.getParent(), false, outputJarLoc));
        btnRefreshVers.addMouseListener(new RefreshListener(forgeVersions));
        btnStart.addMouseListener(new StartListener(inputJarLoc, outputJarLoc, forgeVersions, lblProgressText, masterProgress));
    }

    public static void main(String[] args) {
        addASMToClasspath();
        addGuavaToClasspath();
        JFrame frame = new JFrame("BON2");
        frame.setResizable(false);
        frame.setContentPane(new BON2().contentPane);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private static void addASMToClasspath() {
        try {
            Class.forName("org.objectweb.asm.Opcodes");
        } catch (ClassNotFoundException e) {
            System.out.println("ASM isn't already in classpath. Adding it...");
            if (!ASM_JAR.exists()) {
                throw new RuntimeException("ASM couldn't be found. You must run setupDevWorkspace or setupDecompWorkspace at least once in order to use this.", new FileNotFoundException("asm-debug-all-4.1.jar could not be found"));
            }
            try {
                addUrl(ASM_JAR.toURI().toURL());
                Class.forName("org.objectweb.asm.Opcodes");
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (ClassNotFoundException e1) {
                throw new RuntimeException("ASM couldn't be added to the classpath. Please report to Parker8283.", e1);
            }
        }
    }

    private static void addGuavaToClasspath() {
        try {
            Class.forName("com.google.common.collect.Maps");
        } catch (ClassNotFoundException e) {
            System.out.println("Guava isn't already in classpath. Adding it...");
            if (!GUAVA_JAR.exists()) {
                throw new RuntimeException("Guava couldn't be found. You must run setupDevWorkspace or setupDecompWorkspace at least once in order to use this.", new FileNotFoundException("guava-17.0.jar could not be found"));
            }
            try {
                addUrl(GUAVA_JAR.toURI().toURL());
                Class.forName("com.google.common.collect.Maps");
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (ClassNotFoundException e1) {
                throw new RuntimeException("Guava couldn't be added to the classpath. Please report to Parker8283.", e1);
            }
        }
    }

    private static void addUrl(URL url) {
        URLClassLoader sysloader = (URLClassLoader) BON2.class.getClassLoader();
        Class sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(sysloader, url);
        } catch (Exception e) {
            throw new RuntimeException("Could not add ASM to classpath. Will not continue.", e);
        }
    }
}
