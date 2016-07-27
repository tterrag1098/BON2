package com.github.parker8283.bon2.data;

import java.util.Map;
import java.util.Set;

import gnu.trove.set.hash.TIntHashSet;

public class VersionJson {
    
    public static class MappingsJson {
        
        private TIntHashSet snapshot;
        private TIntHashSet stable;
        
        public boolean hasSnapshot(String version) {
            return hasSnapshot(Integer.valueOf(version));
        }
        
        public boolean hasSnapshot(int version) {
            return snapshot.contains(version); 
        }
        
        public boolean hasStable(String version) {
            return hasStable(Integer.valueOf(version));
        }
        
        public boolean hasStable(int version) {
            return stable.contains(version);
        }
    }
    
    private Map<String, MappingsJson> versionToList;

    public VersionJson(Map<String, MappingsJson> data) {
        this.versionToList = data;
    }

    public MappingsJson getMappings(String mcversion) {
        return versionToList.get(mcversion);
    }
    
    public Set<String> getVersions() {
        return versionToList.keySet();
    }
}
