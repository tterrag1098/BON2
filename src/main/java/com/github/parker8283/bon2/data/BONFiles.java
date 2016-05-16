package com.github.parker8283.bon2.data;

import java.io.File;

public class BONFiles {
    public static File USER_GRADLE_FOLDER = new File(System.getProperty("user.home") + File.separator + ".gradle");
    public static File GRADLE_CACHES_FOLDER;
    public static File CACHES_MINECRAFT_FOLDER;
    public static File MINECRAFT_NET_FOLDER;
    public static File NET_MINECRAFTFORGE_FOLDER;
    public static File MINECRAFTFORGE_FORGE_FOLDER;
    public static File MINECRAFT_DE_FOLDER;
    public static File DE_OCEANLABS_FOLDER;
    public static File OCEANLABS_MCP_FOLDER;
    
    public static void load() {
    	GRADLE_CACHES_FOLDER = new File(USER_GRADLE_FOLDER, "caches");
    	CACHES_MINECRAFT_FOLDER = new File(GRADLE_CACHES_FOLDER, "minecraft");
    	MINECRAFT_NET_FOLDER = new File(CACHES_MINECRAFT_FOLDER, "net");
    	NET_MINECRAFTFORGE_FOLDER = new File(MINECRAFT_NET_FOLDER, "minecraftforge");
    	MINECRAFTFORGE_FORGE_FOLDER = new File(NET_MINECRAFTFORGE_FOLDER, "forge");
    	MINECRAFT_DE_FOLDER = new File(CACHES_MINECRAFT_FOLDER, "de");
    	DE_OCEANLABS_FOLDER = new File(MINECRAFT_DE_FOLDER, "oceanlabs");
    	OCEANLABS_MCP_FOLDER = new File(DE_OCEANLABS_FOLDER, "mcp");
    }

    public static void setUserHome(File userGradle) {
        USER_GRADLE_FOLDER = userGradle;
        load();
    }
}
