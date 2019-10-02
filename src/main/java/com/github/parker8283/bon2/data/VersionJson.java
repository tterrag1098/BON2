package com.github.parker8283.bon2.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VersionJson {

    public static class MappingsJson {

        private int[] snapshot;
        private int[] stable;

        private transient Set<Integer> snapshotSet, stableSet;

        private Set<Integer> getSnapshotSet() {
            if (snapshotSet == null) {
                if (snapshot == null) {
                    snapshotSet = Collections.emptySet();
                } else {
                    snapshotSet = new HashSet<>(snapshot.length);
                    for (int n : snapshot)
                        snapshotSet.add(n);
                }
            }
            return snapshotSet;
        }

        private Set<Integer> getStableSet() {
            if (stableSet == null) {
                if (stable == null) {
                    stableSet = Collections.emptySet();
                } else {
                    stableSet = new HashSet<>(stable.length);
                    for (int n : stable)
                        stableSet.add(n);
                }
            }
            return stableSet;
        }

        public boolean hasSnapshot(String version) {
            return hasSnapshot(Integer.valueOf(version));
        }

        public boolean hasSnapshot(int version) {
            return getSnapshotSet().contains(version);
        }

        public boolean hasStable(String version) {
            return hasStable(Integer.valueOf(version));
        }

        public boolean hasStable(int version) {
            return getStableSet().contains(version);
        }

        public int[] getSnapshots() {
            return snapshot;
        }

        public int[] getStables() {
            return stable;
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
