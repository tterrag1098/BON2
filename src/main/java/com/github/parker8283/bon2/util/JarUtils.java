package com.github.parker8283.bon2.util;

import com.github.parker8283.bon2.srg.ClassCollection;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.tree.ClassNode;

public class JarUtils {

    public static ClassCollection readFromJar(File file) throws IOException {
        List<ClassNode> classes = new ArrayList<ClassNode>();
        Map<String, byte[]> extraFiles = new HashMap<String, byte[]>();
        Manifest manifest = null;
        JarInputStream jin = null;
        try {
            jin = new JarInputStream(new FileInputStream(file), false);
            JarEntry entry;
            while ((entry = jin.getNextJarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName();
                if (name.endsWith(".class")) {
                    ClassNode cn = IOUtils.readClassFromBytes(IOUtils.readStreamFully(jin));
                    if (!name.equals(cn.name + ".class")) {
                        throw new RuntimeException("There was an error in reading a class. Corrupted JAR maybe?", new ClassFormatError(name + " != " + cn.name + ".class"));
                    }
                    classes.add(cn);
                } else {
                    extraFiles.put(name, IOUtils.readStreamFully(jin));
                }
            }
            manifest = jin.getManifest();
        } finally {
            if (jin != null) {
                jin.close();
            }
        }
        return new ClassCollection(classes, manifest, extraFiles);
    }

    public static void writeToJar(ClassCollection cc, File file) throws IOException {
        Set<String> dirs = new HashSet<String>();
        JarOutputStream jout = null;
        try {
            jout = new JarOutputStream(new FileOutputStream(file));
            addDirectories(JarFile.MANIFEST_NAME, dirs);
            jout.putNextEntry(new JarEntry(JarFile.MANIFEST_NAME));
            cc.getManifest().write(jout);
            jout.closeEntry();
            for (ClassNode classNode : cc.getClasses()) {
                addDirectories(classNode.name, dirs);
                jout.putNextEntry(new JarEntry(classNode.name + ".class"));
                jout.write(IOUtils.writeClassToBytes(classNode));
                jout.closeEntry();
            }
            for (Map.Entry<String, byte[]> entry : cc.getExtraFiles().entrySet()) {
                addDirectories(entry.getKey(), dirs);
                jout.putNextEntry(new JarEntry(entry.getKey()));
                jout.write(entry.getValue());
                jout.closeEntry();
            }
            for (String dirPath : dirs) {
                jout.putNextEntry(new JarEntry(dirPath + "/"));
                jout.closeEntry();
            }
            jout.flush();
        } finally {
            if (jout != null) {
                jout.close();
            }
        }
    }

    private static void addDirectories(String filePath, Set<String> dirs) {
        int i = filePath.lastIndexOf('/');
        if (i >= 0) {
            String dirPath = filePath.substring(0, i);
            if (dirs.add(dirPath))
                addDirectories(dirPath, dirs);
        }
    }
}
