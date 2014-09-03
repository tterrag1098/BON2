package com.github.parker8283.bon2.util;

import com.github.parker8283.bon2.srg.ClassCollection;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import org.objectweb.asm.tree.ClassNode;

public class JarReader {

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
}
