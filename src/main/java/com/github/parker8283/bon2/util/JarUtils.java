package com.github.parker8283.bon2.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.*;

import com.github.parker8283.bon2.io.BonJarInputStream;
import org.objectweb.asm.tree.ClassNode;

import com.github.parker8283.bon2.data.IProgressListener;
import com.github.parker8283.bon2.srg.ClassCollection;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class JarUtils {

    public static ClassCollection readFromJar(File file, IProgressListener progress) throws IOException {
        List<ClassNode> classes = Lists.newArrayList();
        Map<String, byte[]> extraFiles = Maps.newHashMap();
        Manifest manifest = null;
        BonJarInputStream jin = null;
        progress.startWithoutProgress("Loading Input JAR");
        try {
            jin = new BonJarInputStream(file, false);
            JarEntry entry;
            while((entry = jin.getNextJarEntry()) != null) {
                if(entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName();
                if(name.endsWith(".class")) {
                    ClassNode cn = IOUtils.readClassFromBytes(IOUtils.readStreamFully(jin));
                    if(!name.equals(cn.name + ".class")) {
                        throw new RuntimeException("There was an error in reading a class. Corrupted JAR maybe?", new ClassFormatError(name + " != " + cn.name + ".class"));
                    }
                    classes.add(cn);
                } else {
                    extraFiles.put(name, IOUtils.readStreamFully(jin));
                }
            }
            manifest = jin.getManifest();
        } finally {
            if(jin != null) {
                jin.close();
            }
        }
        return new ClassCollection(classes, manifest, extraFiles);
    }

    public static void writeToJar(ClassCollection cc, File file, IProgressListener progress) throws IOException {
        int classesWritten = 0;
        Set<String> dirs = Sets.newHashSet();
        JarOutputStream jout = null;
        progress.start(cc.getClasses().size() + cc.getExtraFiles().size() + 1, "Writing remapped JAR");
        try {
            jout = new JarOutputStream(new FileOutputStream(file));

            if(cc.getManifest() != null) {
                addDirectories(JarFile.MANIFEST_NAME, dirs);
                jout.putNextEntry(new JarEntry(JarFile.MANIFEST_NAME));
                cc.getManifest().write(jout);
                jout.closeEntry();
                cc.getExtraFiles().remove(JarFile.MANIFEST_NAME);
            }

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
}
