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
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;

import com.github.parker8283.bon2.data.IErrorHandler;
import com.github.parker8283.bon2.data.IProgressListener;
import com.github.parker8283.bon2.io.FixedJarInputStream;
import com.github.parker8283.bon2.srg.ClassCollection;

public class JarUtils {
    public static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    public static ClassCollection readFromJar(File file, IErrorHandler errorHandler, IProgressListener progress) throws IOException {
        return readFromJar(file, errorHandler, progress, true);
    }

    public static ClassCollection readFromJar(File file, IErrorHandler errorHandler, IProgressListener progress, boolean keepExtra) throws IOException {
        List<ClassNode> classes = new ArrayList<>();
        Map<String, byte[]> extraFiles = new HashMap<>();
        Manifest manifest = null;
        long fileSize = file.length();
        long currentProgress = 0;
        progress.start((int)fileSize, "Loading Input JAR: " + file.getName());

        try (JarInputStream jin = new FixedJarInputStream(file, false)) {
            JarEntry entry;
            while((entry = jin.getNextJarEntry()) != null) {
                if(entry.isDirectory())
                    continue;

                String name = entry.getName();
                if(name.endsWith(".class")) {
                    byte[] bytes = IOUtils.readStreamFully(jin);
                    if(bytes.length > 0) {
                        try {
                            ClassNode cn = readClassFromBytes(bytes);
                            if (!name.equals(cn.name + ".class"))
                                errorHandler.handleError("There was an error in reading a class. Corrupted JAR maybe?\n" + name + " != " + cn.name + ".class", false);
                            else
                                classes.add(cn);
                        } catch (Exception e) {
                            errorHandler.handleError("There was an unexpected error while reading class data. Corrupted JAR maybe?\n" + name, false);
                        }
                    } else {
                        errorHandler.handleError("Found a class with no content. Corrupted JAR maybe?\nClass was:" + name + "\nThe class will be skipped.", true);
                    }
                } else if (keepExtra) {
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
        }
        return new ClassCollection(classes, manifest, extraFiles);
    }

    public static void writeToJar(ClassCollection cc, File file, Remapper remapper, IProgressListener progress) throws IOException {
        if(file.exists()) {
            file.delete();
        }
        int written = 1;
        Manifest manifest = cc.getManifest();
        progress.start(cc.getClasses().size() + cc.getExtraFiles().size() + (manifest == null ? 0 : 1), "Writing remapped JAR");

        try (JarOutputStream jout = new JarOutputStream(new FileOutputStream(file))) {

            Set<String> dirs = new HashSet<>();

            if (manifest != null) {
                startEntry(jout, JarFile.MANIFEST_NAME, dirs);
                manifest.write(jout);
                progress.setProgress(written++);
            }

            for(ClassNode classNode : cc.getClasses()) {
                startEntry(jout, classNode.name + ".class", dirs);
                jout.write(writeClassToBytes(classNode, remapper));
                progress.setProgress(written++);
            }

            for(Map.Entry<String, byte[]> entry : cc.getExtraFiles().entrySet()) {
                startEntry(jout, entry.getKey(), dirs);
                jout.write(entry.getValue());
                progress.setProgress(written++);
            }

            jout.flush();
        }
    }

    private static void startEntry(JarOutputStream jout, String filePath, Set<String> dirs) throws IOException {
        int i = filePath.lastIndexOf('/', filePath.length() - 2);
        if(i != -1) {
            String dir = filePath.substring(0, i + 1);
            if (!dirs.contains(dir)) {
                startEntry(jout, dir, dirs);
                dirs.add(dir);
            }
        }
        jout.putNextEntry(getStableEntry(filePath));
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

    private static byte[] writeClassToBytes(ClassNode classNode, Remapper remapper) {
        ClassWriter writer = new ClassWriter(0);
        ClassVisitor visitor = remapper == null ? writer : new ClassRemapper(writer, remapper);
        classNode.accept(visitor);
        return writer.toByteArray();
    }

    private static ClassNode readClassFromBytes(byte[] bytes) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        return classNode;
    }
}
