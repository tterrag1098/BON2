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
        String v1 = getVersion();
        String v2 = o.getVersion();

        if (v1.equals(v2)) {
            return 0;
        }

        v1 = v1.substring(0, v1.indexOf('-'));
        v2 = v2.substring(0, v2.indexOf('-'));

        if ("unknown".equals(v1)) {
            return -1;
        } else if ("unknown".equals(v2)) {
            return 1;
        }
        
        String[] subv1 = v1.split("\\.");
        String[] subv2 = v2.split("\\.");
        
        for (int i = 0; i < Math.max(subv1.length, subv2.length); i++) {
            if (i >= subv1.length) {
                return -1;
            } else if (i >= subv2.length) {
                return 1;
            }
            int sub1 = Integer.valueOf(subv1[i]);
            int sub2 = Integer.valueOf(subv2[i]);
            if (sub1 != sub2) {
                return sub1 - sub2;
            }
        }
        
        return v1.compareTo(v2);
    }
}

