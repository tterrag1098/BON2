package com.github.parker8283.bon2.data;

import java.io.File;

public class BONFiles {

    public static final File GRADLE_CACHES_FOLDER = new File(System.getenv("GRADLE_USER_HOME") != null ? System.getenv("GRADLE_USER_HOME") : System.getProperty("user.home") + File.separator + ".gradle", "caches");
    public static final File FG3_MC_CACHE = getFolder("forge_gradle/minecraft_repo/versions/");
    public static final File FG3_DOWNLOAD_CACHE = getFolder("forge_gradle/maven_downloader");
    public static final File MINECRAFTFORGE_FORGE_FOLDER = getFolder("minecraft/net/minecraftforge/forge/");
    public static final File OCEANLABS_MCP_FOLDER = getFolder("minecraft/de/oceanlabs/mcp/");

    private static final File getFolder(String path) {
        return new File(GRADLE_CACHES_FOLDER, path.replace("/", File.separator));
    }
}
