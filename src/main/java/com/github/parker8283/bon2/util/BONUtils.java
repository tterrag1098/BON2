package com.github.parker8283.bon2.util;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.parker8283.bon2.data.BONFiles;
import com.google.common.collect.Lists;

public class BONUtils {
    private static final Matcher versionMatcher = Pattern.compile("^\\d+\\.\\d+(\\.\\d+)?(_\\w+)?-\\d+\\.\\d+\\.\\d+\\.\\d+(-.+)?").matcher("");

    public static List<String> buildValidMappings() {
        List<String> versions = Lists.newArrayList();

        File[] fg1_versionFolders = BONFiles.MINECRAFTFORGE_FORGE_FOLDER.listFiles();
        if(fg1_versionFolders != null) {
            List<File> fg1_versions = Lists.newArrayList();
            for(File file : fg1_versionFolders) {
                String name = file.getName();
                if(!name.startsWith("1.6") && versionMatcher.reset(name).matches()) {
                    int mcMinor = Integer.parseInt(name.charAt(2) + "");
                    if(mcMinor == 7) {
                        fg1_versions.add(file);
                    } else if(mcMinor == 8) {
                        String work = name;
                        if(name.lastIndexOf('-') != name.indexOf('-')) {
                            work = name.substring(0, name.lastIndexOf('-'));
                        }
                        if(!(Integer.parseInt(work.substring(work.lastIndexOf('.') + 1)) >= 1503)) {
                            fg1_versions.add(file);
                        }
                    }
                }
            }

            for(File file : fg1_versions) {
                if(new File(file, "srgs").exists()) {
                    versions.add(file.getName() + "-shipped");
                }
                if(hasAdditionalMappings(file)) {
                    List<String> additionalMappings = Lists.newArrayList();
                    additionalMappings = addFG1Mappings(file, "snapshot", additionalMappings);
                    additionalMappings = addFG1Mappings(file, "snapshot_nodoc", additionalMappings);
                    additionalMappings = addFG1Mappings(file, "stable", additionalMappings);
                    additionalMappings = addFG1Mappings(file, "stable_nodoc", additionalMappings);
                    for(String mapping : additionalMappings) {
                        versions.add(file.getName() + "-" + mapping);
                    }
                }
            }
        }

        File[] fg2_mappingsFolders = BONFiles.OCEANLABS_MCP_FOLDER.listFiles();
        if(fg2_mappingsFolders != null) {
            List<File> fg2_versions = Lists.newArrayList();
            for(File file : fg2_mappingsFolders) {
                String name = file.getName();
                if(name.startsWith("mcp_s")) {
                    for(File file1 : file.listFiles()) {
                        if(Arrays.asList(file1.list()).contains("srgs")) {
                            fg2_versions.add(file1);
                        }
                    }
                }
            }

            for(File file : fg2_versions) {
                versions.add("1.8(.8)-" + file.getParentFile().getName().substring(4) + "_" + file.getName()); //TODO Find out mc version for the mappings somehow
            }
        }

        return versions;
    }

    public static File getSrgsFolder(String mappingsVer) {
        if(mappingsVer.startsWith("1.8(.8)-")) {
            String mappingChan = mappingsVer.substring(mappingsVer.indexOf('-') + 1, mappingsVer.lastIndexOf('_'));
            String mappingVer = mappingsVer.substring(mappingsVer.lastIndexOf('_') + 1);
            return new File(BONFiles.OCEANLABS_MCP_FOLDER, "mcp_" + mappingChan + File.separator + mappingVer + File.separator + "srgs");
        } else {
            if(mappingsVer.endsWith("shipped")) {
                return new File(BONFiles.MINECRAFTFORGE_FORGE_FOLDER, mappingsVer.replace("-shipped", "") + File.separator + "srgs");
            } else {
                String forgeVer = mappingsVer.substring(0, mappingsVer.lastIndexOf('-'));
                String mappingChan = mappingsVer.substring(mappingsVer.lastIndexOf('-') + 1, mappingsVer.lastIndexOf('_'));
                String mappingVer = mappingsVer.substring(mappingsVer.lastIndexOf('_') + 1);
                return new File(BONFiles.MINECRAFTFORGE_FORGE_FOLDER, forgeVer + File.separator + mappingChan + File.separator + mappingVer + File.separator + "srgs");
            }
        }
    }

    private static boolean hasAdditionalMappings(File file) {
        return new File(file, "snapshot").exists() || new File(file, "snapshot_nodoc").exists() || new File(file, "stable").exists() || new File(file, "stable_nodoc").exists();
    }

    private static List<String> addFG1Mappings(File rootDir, String channel, List<String> list) {
        File mappingDir = new File(rootDir, channel);
        if(mappingDir.exists()) {
            for(String subdir : mappingDir.list()) {
                list.add(channel + "_" + subdir);
            }
        }
        return list;
    }
}
