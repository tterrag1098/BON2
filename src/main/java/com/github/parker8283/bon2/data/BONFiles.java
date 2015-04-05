package com.github.parker8283.bon2.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class BONFiles {
    public static final File USER_HOME = new File(System.getProperty("user.home"));
    public static final File BON_SAVE_DIR = new File(USER_HOME.getAbsolutePath() + File.separator + ".BON2");
    public static final File FORGE_VERSION_FILE = new File(BON_SAVE_DIR.getAbsolutePath() + File.separator + "forge_version.txt");
    public static final File USER_GRADLE_FOLDER = new File(USER_HOME.getAbsolutePath() + File.separator + ".gradle");
    public static final File GRADLE_CACHES_FOLDER = new File(USER_GRADLE_FOLDER, "caches");
    public static final File CACHES_MINECRAFT_FOLDER = new File(GRADLE_CACHES_FOLDER, "minecraft");
    public static final File MINECRAFT_NET_FOLDER = new File(CACHES_MINECRAFT_FOLDER, "net");
    public static final File NET_MINECRAFTFORGE_FOLDER = new File(MINECRAFT_NET_FOLDER, "minecraftforge");
    public static final File MINECRAFTFORGE_FORGE_FOLDER = new File(NET_MINECRAFTFORGE_FOLDER, "forge");
    public static final File NET_MINECRAFT_FOLDER = new File(MINECRAFT_NET_FOLDER, "minecraft");
    public static final File CACHES_MODULES_FOLDER = new File(GRADLE_CACHES_FOLDER, "modules-2");
    public static final File MODULES_FILES_FOLDER = new File(CACHES_MODULES_FOLDER, "files-2.1");

    static {
        BON_SAVE_DIR.mkdirs();
        try {
            FORGE_VERSION_FILE.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getSavedForgeVersion() {
        Scanner scan = null;
        try {
            scan = new Scanner(FORGE_VERSION_FILE);
            return scan.nextLine();
        } catch (Exception e) {
            return null;
        } finally {
            if (scan != null) {
                scan.close();
            }
        }
    }

    public static void saveForgeVersion(String ver) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(FORGE_VERSION_FILE);
            fw.write(ver);
            fw.close();
        } catch (IOException e) {
            ;
        }
    }
}
