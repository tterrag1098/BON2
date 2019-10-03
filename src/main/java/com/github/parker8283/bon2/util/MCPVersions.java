package com.github.parker8283.bon2.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.gson.annotations.SerializedName;

import net.minecraftforge.srgutils.MinecraftVersion;

public class MCPVersions {
    private static final String FORGE_MAVEN = "http://files.minecraftforge.net/maven/";
    private static final String MCP_ROOT = "de/oceanlabs/mcp/mcp/";
    private static final String MCP_CONFIG_ROOT = "de/oceanlabs/mcp/mcp_config/";

    private static List<MCPVersion> knownVersions;

    public static MCPVersion get(MinecraftVersion mcver) {
        if (mcver == null)
            return null;
        for (MCPVersion v : getKnownVersions()) {
            if (v.toString().equals(mcver.toString()))
                return v;
        }
        return null;
    }

    public static List<MCPVersion> getKnownVersions() {
        return getKnownVersions(false);
    }
    public static List<MCPVersion> getKnownVersions(boolean force) {
        if (!force && knownVersions != null)
            return knownVersions;

        Set<MCPVersion> versions = new HashSet<>();
        versions.addAll(findMcp());
        versions.addAll(findMcpConfig());
        //TODO: Other discovery?
        knownVersions = Collections.unmodifiableList(versions.stream().sorted().collect(Collectors.toList()));
        return knownVersions;
    }

    private static Set<MCPVersion> findMcp() {
        return findFromForgeMaven(new File("data/mcp_manifest.json"), FORGE_MAVEN + MCP_ROOT, MCPVersionOld::new);
    }
    private static Set<MCPVersion> findMcpConfig() {
        return findFromForgeMaven(new File("data/mcp_config_manifest.json"), FORGE_MAVEN + MCP_CONFIG_ROOT, MCPVersionNew::new);
    }

    private static Set<MCPVersion> findFromForgeMaven(File cache, String root, Function<String, ? extends MCPVersion> factory) {
        try {
            if (!DownloadUtils.downloadWithCache(new URL(root + "maven-metadata.json"), cache, false, false)) {
                System.out.println("Failed to download MCP Version manifest from: " + root + "maven-metadata.json");
                return Collections.emptySet();
            }

            Manifest manifest = IOUtils.loadJson(cache, Manifest.class);
            if (manifest == null || manifest.versions == null)
                return Collections.emptySet();

            Set<MCPVersion> versions = new HashSet<>();
            for (String v : manifest.versions) {
                versions.add(factory.apply(v));
            }
            return versions;
        } catch (IOException e) {
            System.out.println("Failed to download Version manifest from: " + root + "maven-metadata.json");
            e.printStackTrace();
            return Collections.emptySet();
        }
    }

    public static abstract class MCPVersion implements Comparable<MCPVersion> {
        protected final MinecraftVersion mcver;
        protected final String url;

        protected MCPVersion(String mcver, String url) {
            this.mcver = MinecraftVersion.from(mcver);
            this.url = url;
        }

        public String getUrl() {
            return this.url;
        }

        public abstract File getTarget(File cacheRoot);
        public abstract String getMappings(File data);

        @Override
        public int compareTo(MCPVersion o) {
            return this.mcver.compareTo(o.mcver);
        }

        @Override
        public String toString() {
            return this.mcver.toString();
        }

        @Override
        public int hashCode() {
            return this.mcver.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof MCPVersion))
                return false;
            return this.mcver.equals(((MCPVersion)o).mcver);
        }
    }

    public static class MCPVersionOld extends MCPVersion {
        protected MCPVersionOld(String ver) {
            super(ver, FORGE_MAVEN + MCP_ROOT + ver + "/mcp-" + ver + "-csrg.zip");
        }

        @Override
        public File getTarget(File cacheRoot) {
            return new File(cacheRoot, FORGE_MAVEN + MCP_ROOT + mcver + "/mcp-" + mcver + "-csrg.zip");
        }

        @Override
        public String getMappings(File data) {
            return "joined.csrg";
        }
    }

    public static class MCPVersionNew extends MCPVersion {
        private String configName;

        protected MCPVersionNew(String ver) {
            super(ver, FORGE_MAVEN + MCP_CONFIG_ROOT + ver + "/mcp_config-" + ver + ".zip");
        }

        @Override
        public File getTarget(File cacheRoot) {
            return new File(cacheRoot, MCP_CONFIG_ROOT + mcver + "/mcp_config-" + mcver + ".zip");
        }

        @Override
        public String getMappings(File data) {
            if (configName != null)
                return configName;

            try {
                byte[] cfgData = IOUtils.getZipData(data, "config.json");
                Speced spec = IOUtils.loadJson(cfgData, Speced.class);
                if (spec.spec != 1) {
                    System.out.println("Failed to read MCPConfig data from: " + data.getAbsolutePath() + " Unknown spec: " + spec.spec);
                    return null;
                }

                MCPConfigV1 v1 = IOUtils.loadJson(cfgData, MCPConfigV1.class);
                configName = v1.getData("mappings");
                if (configName == null) {
                    System.out.println("Failed to read MCPConfig data from: " + data.getAbsolutePath() + " Missing 'mappings' data entry");
                    return null;
                }

                return configName;
            } catch (IOException e) {
                System.out.println("Failed to read MCPConfig data from: " + data.getAbsolutePath());
                e.printStackTrace();
                return null;
            }
        }
    }

    public static class Manifest {
        @SerializedName("default")
        public List<String> versions;
    }

    public static class Speced {
        public int spec;
    }

    public static class MCPConfigV1 extends Speced {

        private Map<String, Object> data;

        @SuppressWarnings("unchecked")
        public String getData(String... path) {
            if (data == null)
                return null;
            Map<String, Object> level = data;
            for (String part : path) {
                if (!level.containsKey(part))
                    return null;
                Object val = level.get(part);
                if (val instanceof String)
                    return (String)val;
                if (val instanceof Map)
                    level = (Map<String, Object>)val;
            }
            return null;
        }
    }
}
