package com.github.parker8283.bon2.data;

import java.io.File;

public class BONFiles {

    public static final File USER_GRADLE_FOLDER = new File(System.getenv("GRADLE_USER_HOME") != null ? System.getenv("GRADLE_USER_HOME") : System.getProperty("user.home") + File.separator + ".gradle");
    public static final File GRADLE_CACHES_FOLDER = new File(USER_GRADLE_FOLDER, "caches");
    public static final File CACHES_MINECRAFT_FOLDER = new File(GRADLE_CACHES_FOLDER, "minecraft");
    public static final File MINECRAFT_NET_FOLDER = new File(CACHES_MINECRAFT_FOLDER, "net");
    public static final File NET_MINECRAFTFORGE_FOLDER = new File(MINECRAFT_NET_FOLDER, "minecraftforge");
    public static final File MINECRAFTFORGE_FORGE_FOLDER = new File(NET_MINECRAFTFORGE_FOLDER, "forge");
    public static final File MINECRAFT_DE_FOLDER = new File(CACHES_MINECRAFT_FOLDER, "de");
    public static final File DE_OCEANLABS_FOLDER = new File(MINECRAFT_DE_FOLDER, "oceanlabs");
    public static final File OCEANLABS_MCP_FOLDER = new File(DE_OCEANLABS_FOLDER, "mcp");

}
