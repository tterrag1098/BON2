package com.github.parker8283.bon2.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.parker8283.bon2.data.BONFiles;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import net.minecraftforge.srgutils.MinecraftVersion;

public class MappingVersions {

    private static final String VERSION_JSON = "http://export.mcpbot.bspk.rs/versions.json";
    private static final String FORGE_MAVEN = "http://files.minecraftforge.net/maven/";
    private static final String MCP_ROOT = "de/oceanlabs/mcp/";

    private static Map<MinecraftVersion, List<MappingVersion>> knownVersions;

    public static List<MappingVersion> getExistingVersions() {
        List<MappingVersion> ret = new ArrayList<>();
        getKnownVersions().forEach((k,v) -> v.stream().filter(e -> e.getTarget(BONFiles.FG3_DOWNLOAD_CACHE).exists()).forEach(ret::add));
        return ret;
    }

    public static Map<MinecraftVersion, List<MappingVersion>> getKnownVersions() {
        return getKnownVersions(false);
    }
    public static Map<MinecraftVersion, List<MappingVersion>> getKnownVersions(boolean force) {
        if (!force && knownVersions != null)
            return knownVersions;

        Map<MinecraftVersion, Set<MappingVersion>> versions = new HashMap<>();
        merge(versions, findBotExport());
        //MCPBot does not publish as a real maven artifact, so we can't use the standard Forge Maven metadata system.
        //merge(versions, findForgeSnapshot());
        //merge(versions, findForgeStable());
        //TODO: Official Mappings

        Map<MinecraftVersion, List<MappingVersion>> ret = new TreeMap<>(); //TreeMaps are sorted...
        versions.forEach((k,v) -> ret.put(k, Collections.unmodifiableList(v.stream().sorted().collect(Collectors.toList()))));
        knownVersions = Collections.unmodifiableMap(ret);
        return knownVersions;
    }

    public static MappingVersion getFromString(String desc) {
        desc = desc.toLowerCase(Locale.ENGLISH);
        if (desc.startsWith("stable_nodoc_")) {
            return new StableMappingVersion(desc.substring(13));
        } else if (desc.startsWith("stable_")) {
            return new StableMappingVersion(desc.substring(7));
        } else if (desc.startsWith("snapshot_nodoc_")) {
            return new SnapshotMappingVersion(desc.substring(15));
        } else if (desc.startsWith("snapshot_")) {
            return new SnapshotMappingVersion(desc.substring(9));
        }
        //TODO: Official
        return null;
    }

    private static void merge(Map<MinecraftVersion, Set<MappingVersion>> master, Map<MinecraftVersion, Set<MappingVersion>> extra) {
        extra.forEach((k,v) -> master.computeIfAbsent(k, x -> new HashSet<>()).addAll(v));
    }

    private static Map<MinecraftVersion, Set<MappingVersion>> findBotExport() {
        File target = new File("data/bot_manifest.json");
        try {
            if (!DownloadUtils.downloadFile(new URL(VERSION_JSON), target, false)) {
                System.out.println("Failed to download MCP Version manifest from: " + VERSION_JSON);
                return Collections.emptyMap();
            }
            Map<String, BotManifestEntry> data = IOUtils.loadJson(target, new TypeToken<Map<String, BotManifestEntry>>() {}.getType());
            Map<MinecraftVersion, Set<MappingVersion>> ret = new HashMap<>();
            data.forEach((k,v) -> {
                Set<MappingVersion> set = new HashSet<>();
                if (v.snapshot != null) {
                    for (int id : v.snapshot)
                        set.add(new SnapshotMappingVersion(id + "-" + k));
                }
                if (v.stable != null) {
                    for (int id : v.stable)
                        set.add(new StableMappingVersion(id + "-" + k));
                }
                ret.put(MinecraftVersion.from(k), set);
            });
            return ret;
        } catch (IOException e) {
            System.out.println("Failed to download Version manifest from: " + VERSION_JSON);
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    @SuppressWarnings("unused")
    private static Map<MinecraftVersion, Set<MappingVersion>> findForgeStable() {
        return findFromForgeMaven(new File("data/mcp_stable_nodoc_manifest.json"), FORGE_MAVEN + MCP_ROOT + "mcp_stable_nodoc/", StableMappingVersion::new);
    }

    @SuppressWarnings("unused")
    private static Map<MinecraftVersion, Set<MappingVersion>> findForgeSnapshot() {
        return findFromForgeMaven(new File("data/mcp_snapshot_nodoc_manifest.json"), FORGE_MAVEN + MCP_ROOT + "mcp_snapshot_nodoc/", SnapshotMappingVersion::new);
    }

    private static Map<MinecraftVersion, Set<MappingVersion>> findFromForgeMaven(File cache, String root, Function<String, ? extends MappingVersion> factory) {
        try {
            if (!DownloadUtils.downloadWithCache(new URL(root + "maven-metadata.json"), cache, false, false)) {
                System.out.println("Failed to download MCP Version manifest from: " + root + "maven-metadata.json");
                return Collections.emptyMap();
            }

            MavenManifest manifest = IOUtils.loadJson(cache, MavenManifest.class);
            if (manifest == null || manifest.versions == null)
                return Collections.emptyMap();

            Map<MinecraftVersion, Set<MappingVersion>> versions = new HashMap<>();
            for (String v : manifest.versions) {
                MappingVersion mv = factory.apply(v);
                versions.computeIfAbsent(mv.getMCVersion(), k -> new HashSet<>()).add(mv);
            }
            return versions;
        } catch (IOException e) {
            System.out.println("Failed to download Version manifest from: " + root + "maven-metadata.json");
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    public static class BotManifestEntry {
        public int[] stable;
        public int[] snapshot;
    }

    public static class MavenManifest {
        @SerializedName("default")
        public List<String> versions;
    }

    public static enum Type {
        SNAPSHOT, STABLE, OFFICIAL;
    }

    public static abstract class MappingVersion implements Comparable<MappingVersion> {
        protected final Type type;
        protected final MinecraftVersion mcver;
        protected final int version;
        protected final String url;
        protected final String path;

        protected MappingVersion(Type type, String ver, String url, String path) {
            this.type = type;
            String[] pts = ver.split("-");
            this.version = Integer.parseInt(pts[0]);
            this.mcver = MinecraftVersion.from(pts[1]);
            this.url = url + path;
            this.path = path;
        }

        public Type getType() {
            return type;
        }

        public String getUrl() {
            return this.url;
        }

        public int getVersion() {
            return version;
        }

        public MinecraftVersion getMCVersion() {
            return this.mcver;
        }

        public File getTarget(File cacheRoot) {
            return new File(cacheRoot, path);
        }

        @Override
        public int compareTo(MappingVersion o) {
            int ret = this.mcver.compareTo(o.mcver);
            if (ret != 0) return ret;
            ret = this.getType().compareTo(o.getType());
            if (ret != 0) return ret;
            return this.getVersion() - o.getVersion();
        }

        @Override
        public String toString() {
            return this.type.name().toLowerCase() + '_' + this.version + '-' + this.mcver;
        }

        @Override
        public int hashCode() {
            int hash = this.mcver.hashCode();
            hash = 31 * hash + this.type.hashCode();
            hash = 32 * hash + version;
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof MappingVersion))
                return false;
            MappingVersion om = (MappingVersion)o;
            return this.mcver.equals(om.mcver) && this.type == om.type && this.version == om.version;
        }
    }

    public static class StableMappingVersion extends MappingVersion {
        protected StableMappingVersion(String ver) {
            super(Type.STABLE, ver, FORGE_MAVEN, MCP_ROOT + "mcp_stable_nodoc/" + ver + "/mcp_stable_nodoc-" + ver + ".zip");
        }
    }

    public static class SnapshotMappingVersion extends MappingVersion {
        protected SnapshotMappingVersion(String ver) {
            super(Type.SNAPSHOT, ver, FORGE_MAVEN, MCP_ROOT + "mcp_snapshot_nodoc/" + ver + "/mcp_snapshot_nodoc-" + ver + ".zip");
        }
    }
}
