package com.github.parker8283.bon2.srg;

import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;

import org.objectweb.asm.tree.ClassNode;

public class ClassCollection {
    private List<ClassNode> classes;
    private Manifest manifest;
    private Map<String, byte[]> extraFiles;

    public ClassCollection(List<ClassNode> classes, Manifest manifest, Map<String, byte[]> extraFiles) {
        this.classes = classes;
        this.manifest = manifest;
        this.extraFiles = extraFiles;
    }

    public List<ClassNode> getClasses() {
        return classes;
    }

    public Manifest getManifest() {
        return manifest;
    }

    public Map<String, byte[]> getExtraFiles() {
        return extraFiles;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }

        ClassCollection that = (ClassCollection)o;

        if(classes != null ? !classes.equals(that.classes) : that.classes != null) {
            return false;
        }
        if(extraFiles != null ? !extraFiles.equals(that.extraFiles) : that.extraFiles != null) {
            return false;
        }
        if(manifest != null ? !manifest.equals(that.manifest) : that.manifest != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = classes != null ? classes.hashCode() : 0;
        result = 31 * result + (manifest != null ? manifest.hashCode() : 0);
        result = 31 * result + (extraFiles != null ? extraFiles.hashCode() : 0);
        return result;
    }
}
