package com.github.parker8283.bon2.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.parker8283.bon2.util.MinecraftVersions.Manifest.Version;
import com.github.parker8283.bon2.util.MinecraftVersions.VersionConfig.Download;

import net.minecraftforge.srgutils.MinecraftVersion;

public class MinecraftVersions {
    private static final String MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    private static List<MinecraftVersion> knownVersions;
    private static Map<MinecraftVersion, URL> versionUrls = new HashMap<>();

    public static List<MinecraftVersion> getKnownVersions() {
        return getKnownVersions(false);
    }
    public static List<MinecraftVersion> getKnownVersions(boolean force) {
        if (!force && knownVersions != null)
            return knownVersions;

        Set<MinecraftVersion> versions = new HashSet<>();
        versions.addAll(findFromLauncherManifest());
        //TODO: Other discovery?
        knownVersions = Collections.unmodifiableList(versions.stream().sorted().collect(Collectors.toList()));
        return knownVersions;
    }

    public static MappingsUrls getPGUrls(MinecraftVersion version) {
        File versionF = new File("data/versions/" + version + "/version.json");
        URL url = versionUrls.get(version);
        if (url == null) {
            System.out.println("Unknown version specific url for Minecraft Version: " + version);
            return null;
        }
        try {
            if (!DownloadUtils.downloadEtag(url, versionF, false)) {
                System.out.println("Failed to download version configuration from: " + url);
                return null;
            }
            VersionConfig cfg = IOUtils.loadJson(versionF, VersionConfig.class);
            if (cfg.downloads == null) {
                System.out.println("Failed to find mapping url, version config missing download section" + url);
                return null;
            }

            MappingsUrls ret = new MappingsUrls();
            Download dep = cfg.downloads.get("client_mappings");
            if (dep == null || dep.url == null)
                System.out.println("Failed to find mapping url, version \"" + version + "\" config missing download info for \"client_mappings\"");
            else
                ret.client = dep.url;


            dep = cfg.downloads.get("server_mappings");
            if (dep == null || dep.url == null)
                System.out.println("Failed to find mapping url, version \"" + version + "\" config missing download info for \"server_mappings\"");
            else
                ret.server = dep.url;

            return ret.client == null && ret.server == null ? null : ret;
        } catch (IOException e) {
            System.out.println("Failed to download version configuration from: " + url);
            e.printStackTrace();
            return null;
        }
    }

    private static Set<MinecraftVersion> findFromLauncherManifest() {
        File manifestF = new File("data/version_manifest.json");
        try {
            if (!DownloadUtils.downloadEtag(new URL(MANIFEST_URL), manifestF, false)) {
                System.out.println("Failed to download Minecraft Version manifest from: " + MANIFEST_URL);
                return Collections.emptySet();
            }

            Manifest manifest = IOUtils.loadJson(manifestF, Manifest.class);
            if (manifest == null || manifest.versions == null)
                return Collections.emptySet();

            Set<MinecraftVersion> versions = new HashSet<>();
            for (Version v : manifest.versions) {
                if (!"release".equals(v.type))
                    continue; //TODO: Support selecting snapshots/finding the MCP version from that?
                MinecraftVersion ver = MinecraftVersion.from(v.id);
                versions.add(ver);
                if (v.url != null)
                    versionUrls.put(ver, v.url);
            }
            return versions;
        } catch (IOException e) {
            System.out.println("Failed to download Version manifest from: " + MANIFEST_URL);
            e.printStackTrace();
            return Collections.emptySet();
        }
    }

    public static class Manifest {
        public static class Version {
            public String id;
            public String type;
            public URL url;
        }

        public List<Version> versions;
    }

    public static class VersionConfig {
        public Map<String, Download> downloads;

        public static class Download {
            public URL url;
        }
    }

    public static class MappingsUrls {
        public URL client;
        public URL server;
    }
}
