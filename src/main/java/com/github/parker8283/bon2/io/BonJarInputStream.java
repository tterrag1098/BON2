package com.github.parker8283.bon2.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * Created by clienthax on 11/12/2014.
 */
public class BonJarInputStream extends JarInputStream {

    Manifest manifest = null;

    public BonJarInputStream(File in, boolean verify) throws IOException {
        super(new FileInputStream(in), verify);

        JarFile jarFile = new JarFile(in);
        JarEntry manifestEntry = jarFile.getJarEntry(JarFile.MANIFEST_NAME);
        this.manifest = new Manifest(jarFile.getInputStream(manifestEntry));

    }

    @Override
    public Manifest getManifest() {
        return this.manifest;
    }

}
