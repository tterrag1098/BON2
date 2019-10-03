package com.github.parker8283.bon2.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.jar.*;
import java.util.zip.ZipEntry;

import org.objectweb.asm.tree.ClassNode;

import com.github.parker8283.bon2.data.IErrorHandler;
import com.github.parker8283.bon2.data.IProgressListener;
import com.github.parker8283.bon2.io.FixedJarInputStream;
import com.github.parker8283.bon2.srg.ClassCollection;

public class JarUtils {
    public static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    public static ClassCollection readFromJar(File file, IErrorHandler errorHandler, IProgressListener progress) throws IOException {
        List<ClassNode> classes = new ArrayList<>();
        Map<String, byte[]> extraFiles = new HashMap<>();
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
                    byte[] bytes = IOUtils.readStreamFully(jin);
                    if(bytes.length > 0) {
                        ClassNode cn = null;
                        try {
                            cn = IOUtils.readClassFromBytes(bytes);

                            if(!name.equals(cn.name + ".class")) {
                                errorHandler.handleError("There was an error in reading a class. Corrupted JAR maybe?\n" + name + " != " + cn.name + ".class", false);
                            } else {
                                classes.add(cn);
                            }
                        } catch (Exception e) {
                            errorHandler.handleError("There was an unexpected error while reading class data. Corrupted JAR maybe?\n" + name, false);
                        }
                    } else {
                        errorHandler.handleError("Found a class with no content. Corrupted JAR maybe?\nClass was:" + name + "\nThe class will be skipped.", true);
                    }
                } else {
                    String upperCaseName = name.toUpperCase(Locale.ROOT);
                    // Skip MANIFEST, since it's handled specially, and any signature files as they will be invalid after modifying binaries
                    if (!upperCaseName.startsWith("META-INF/") || (!upperCaseName.endsWith("MANIFEST.MF") && !upperCaseName.endsWith(".SF") && !upperCaseName.endsWith(".RSA"))) {
                        extraFiles.put(name, IOUtils.readStreamFully(jin));
                    }
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
        Set<String> dirs = new HashSet<>();
        JarOutputStream jout = null;
        progress.start(cc.getClasses().size() + cc.getExtraFiles().size() + 1, "Writing remapped JAR");
        try {
            jout = new JarOutputStream(new FileOutputStream(file));
            addDirectories(JarFile.MANIFEST_NAME, dirs);
            jout.putNextEntry(new JarEntry(JarFile.MANIFEST_NAME));
            Manifest manifest = cc.getManifest();
            if (manifest != null) {
                manifest.write(jout);
            }
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
        if (manifestIn == null) {
            return manifestIn;
        }
        Manifest manifestOut = new Manifest(manifestIn);
        for(Map.Entry<String, Attributes> entry : manifestIn.getEntries().entrySet()) {
            manifestOut.getEntries().remove(entry.getKey());
        }
        return manifestOut;
    }

    public static ZipEntry getStableEntry(String name) {
        TimeZone _default = TimeZone.getDefault();
        TimeZone.setDefault(GMT);
        ZipEntry ret = new ZipEntry(name);
        ret.setTime(628041600000L);
        TimeZone.setDefault(_default);
        return ret;
    }
}
