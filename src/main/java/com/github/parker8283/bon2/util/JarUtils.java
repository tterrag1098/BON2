package com.github.parker8283.bon2.util;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.*;

import javax.swing.JOptionPane;

import org.objectweb.asm.tree.ClassNode;

import com.github.parker8283.bon2.BON2;
import com.github.parker8283.bon2.data.IProgressListener;
import com.github.parker8283.bon2.io.FixedJarInputStream;
import com.github.parker8283.bon2.srg.ClassCollection;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class JarUtils {

    public static ClassCollection readFromJar(Component parent, File file, IProgressListener progress) throws IOException {
        List<ClassNode> classes = Lists.newArrayList();
        Map<String, byte[]> extraFiles = Maps.newHashMap();
        Manifest manifest = null;
        FixedJarInputStream jin = null;
        long fileSize = file.length();
        long currentProgress = 0;
        progress.start((int)fileSize, "Loading Input JAR");
        try {
            jin = new FixedJarInputStream(file, false);
            JarEntry entry;
            while((entry = jin.getNextJarEntry()) != null) {
                if(entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName();
                if(name.endsWith(".class")) {
                    System.out.println(name);
                    byte[] bytes = IOUtils.readStreamFully(jin);
                    if(bytes.length > 0) {
                        ClassNode cn = IOUtils.readClassFromBytes(bytes);
                        if(!name.equals(cn.name + ".class")) {
                            JOptionPane.showMessageDialog(parent, "There was an error in reading a class. Corrupted JAR maybe?\n" + name + " != " + cn.name + ".class", BON2.ERROR_DIALOG_TITLE, JOptionPane.ERROR_MESSAGE);
                        } else {
                            classes.add(cn);
                        }
                    } else {
                        JOptionPane.showMessageDialog(parent, "Found a class with no content. Corrupted JAR maybe?\nClass was:" + name + "\nThe class will be skipped.", BON2.ERROR_DIALOG_TITLE, JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    if(name.startsWith("META-INF")) continue;
                    extraFiles.put(name, IOUtils.readStreamFully(jin));
                }
                progress.setProgress((int)(currentProgress += entry.getCompressedSize()));
            }
            manifest = stripManifest(jin.getManifest());
            progress.setProgress((int)fileSize);
        } finally {
            if(jin != null) {
                jin.close();
            }
        }
        return new ClassCollection(classes, manifest, extraFiles);
    }

    public static void writeToJar(ClassCollection cc, File file, IProgressListener progress) throws IOException {
        if(file.exists()) {
            file.delete();
        }
        int classesWritten = 0;
        Set<String> dirs = Sets.newHashSet();
        JarOutputStream jout = null;
        progress.start(cc.getClasses().size() + cc.getExtraFiles().size() + 1, "Writing remapped JAR");
        try {
            jout = new JarOutputStream(new FileOutputStream(file));
            addDirectories(JarFile.MANIFEST_NAME, dirs);
            jout.putNextEntry(new JarEntry(JarFile.MANIFEST_NAME));
            cc.getManifest().write(jout);
            jout.closeEntry();
            progress.setProgress(++classesWritten);
            for(ClassNode classNode : cc.getClasses()) {
                addDirectories(classNode.name, dirs);
                jout.putNextEntry(new JarEntry(classNode.name + ".class"));
                jout.write(IOUtils.writeClassToBytes(classNode));
                jout.closeEntry();
                progress.setProgress(++classesWritten);
            }
            for(Map.Entry<String, byte[]> entry : cc.getExtraFiles().entrySet()) {
                addDirectories(entry.getKey(), dirs);
                jout.putNextEntry(new JarEntry(entry.getKey()));
                jout.write(entry.getValue());
                jout.closeEntry();
                progress.setProgress(++classesWritten);
            }
            for(String dirPath : dirs) {
                jout.putNextEntry(new JarEntry(dirPath + "/"));
                jout.closeEntry();
            }
            jout.flush();
        } finally {
            if(jout != null) {
                jout.close();
            }
        }
    }

    private static void addDirectories(String filePath, Set<String> dirs) {
        int i = filePath.lastIndexOf('/');
        if(i >= 0) {
            String dirPath = filePath.substring(0, i);
            if(dirs.add(dirPath)) {
                addDirectories(dirPath, dirs);
            }
        }
    }

    private static Manifest stripManifest(Manifest manifestIn) {
        Manifest manifestOut = new Manifest(manifestIn);
        for(Map.Entry<String, Attributes> entry : manifestIn.getEntries().entrySet()) {
            manifestOut.getEntries().remove(entry.getKey());
        }
        return manifestOut;
    }
}
