package com.github.parker8283.bon2.data;

import java.io.File;

import com.google.common.base.Preconditions;

public class MappingVersion implements Comparable<MappingVersion> {

    private String version;
    private File srgs;
    
    public MappingVersion(String version, File srgs) {
        Preconditions.checkNotNull(version);
        Preconditions.checkNotNull(srgs);
        Preconditions.checkArgument(srgs.exists() && srgs.isDirectory(), "Folder for srgs does not exist or is not a directory.");
        
        this.version = version;
        this.srgs = srgs;
    }

    public File getSrgs() {
        return srgs;
    }

    public String getVersion() {
        return version;
    }
    
    @Override
    public String toString() {
        return version;
    }

    @Override
    public int hashCode() {
        return version.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MappingVersion other = (MappingVersion) obj;
        return getVersion().equals(other.getVersion());
    }

    @Override
    public int compareTo(MappingVersion o) {
        return getVersion().compareTo(o.getVersion());
    }
}
