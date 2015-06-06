package com.github.parker8283.bon2.util;

import com.github.parker8283.bon2.data.BONFiles;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BONUtils {
    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+(\\.\\d+)?(_\\w+)?-\\d+\\.\\d+\\.\\d+\\.\\d+(-.+)?");
    private static final Matcher versionMatcher = VERSION_PATTERN.matcher("");

    public static List<String> buildValidMappings() {
        File[] versionFolders = BONFiles.MINECRAFTFORGE_FORGE_FOLDER.listFiles();
        if(versionFolders == null) {
            return Collections.emptyList();
        }
        List<File> acceptedVersions = Lists.newArrayList();
        for(File file : versionFolders) {
            String name = file.getName();
            if(!name.startsWith("1.6") && versionMatcher.reset(name).matches()) {
                acceptedVersions.add(file);
            }
        }
        List<String> versions = Lists.newArrayList();
        for(File file : acceptedVersions) {
            if(new File(file, "srgs").exists()) {
                versions.add(file.getName() + "-shipped");
            }
            if(hasAdditionalMappings(file)) {
                List<String> additionalMappings = Lists.newArrayList();
                File mappingDir = new File(file, "snapshot");
                if(mappingDir.exists()) {
                    for(String date : mappingDir.list()) {
                        additionalMappings.add("snapshot_" + date);
                    }
                }
                mappingDir = new File(file, "snapshot_nodoc");
                if(mappingDir.exists()) {
                    for(String date : mappingDir.list()) {
                        additionalMappings.add("snapshot_nodoc_" + date);
                    }
                }
                mappingDir = new File(file, "stable");
                if(mappingDir.exists()) {
                    for(String date : mappingDir.list()) {
                        additionalMappings.add("stable_" + date);
                    }
                }
                mappingDir = new File(file, "stable_nodoc");
                if(mappingDir.exists()) {
                    for(String date : mappingDir.list()) {
                        additionalMappings.add("stable_nodoc_" + date);
                    }
                }
                for(String mapping : additionalMappings) {
                    versions.add(file.getName() + "-" + mapping);
                }
            }
        }
        return versions;
    }

    public static File getSrgsFolder(String mappingsVer) {
        if(mappingsVer.endsWith("shipped")) {
            return new File(BONFiles.MINECRAFTFORGE_FORGE_FOLDER, mappingsVer.replace("-shipped", "") + File.separator + "srgs");
        } else {
            String forgeVer = mappingsVer.substring(0, mappingsVer.lastIndexOf('-'));
            String mappingChan = mappingsVer.substring(mappingsVer.lastIndexOf('-') + 1, mappingsVer.lastIndexOf('_'));
            String mappingVer = mappingsVer.substring(mappingsVer.lastIndexOf('_') + 1);
            return new File(BONFiles.MINECRAFTFORGE_FORGE_FOLDER, forgeVer + File.separator + mappingChan + File.separator + mappingVer + File.separator + "srgs");
        }
    }

    private static boolean hasAdditionalMappings(File file) {
        return new File(file, "snapshot").exists() || new File(file, "snapshot_nodoc").exists() || new File(file, "stable").exists() || new File(file, "stable_nodoc").exists();
    }
}
