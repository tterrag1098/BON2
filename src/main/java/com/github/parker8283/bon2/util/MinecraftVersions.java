package com.github.parker8283.bon2.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.parker8283.bon2.util.MinecraftVersions.Manifest.Version;

import net.minecraftforge.srgutils.MinecraftVersion;

public class MinecraftVersions {
    private static final String MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    private static List<MinecraftVersion> knownVersions;

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
                versions.add(MinecraftVersion.from(v.id));
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
}
