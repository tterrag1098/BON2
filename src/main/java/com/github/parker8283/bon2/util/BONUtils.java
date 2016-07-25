package com.github.parker8283.bon2.util;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.parker8283.bon2.data.BONFiles;
import com.github.parker8283.bon2.data.MappingVersion;
import com.github.parker8283.bon2.data.VersionLookup;
import com.google.common.collect.Lists;

public class BONUtils {
    private static final Matcher versionMatcher = Pattern.compile("^\\d+\\.\\d+(\\.\\d+)?(_\\w+)?-\\d+\\.\\d+\\.\\d+\\.\\d+(-.+)?").matcher("");

    public static List<MappingVersion> buildValidMappings() {
        List<MappingVersion> versions = Lists.newArrayList();

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
                File srgs = new File(file, "srgs");
                if(srgs.exists()) {
                    versions.add(new MappingVersion(file.getName() + "-shipped", srgs));
                }
                if(hasAdditionalMappings(file)) {
                    List<MappingVersion> additionalMappings = Lists.newArrayList();
                    addFG1Mappings(file, "snapshot", additionalMappings);
                    addFG1Mappings(file, "snapshot_nodoc", additionalMappings);
                    addFG1Mappings(file, "stable", additionalMappings);
                    addFG1Mappings(file, "stable_nodoc", additionalMappings);
                    versions.addAll(additionalMappings);
                    //file.getName() + "-" + mapping);
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
                String version = VersionLookup.INSTANCE.getVersionFor(file.getName()) + "-" + file.getParentFile().getName().substring(4) + "_" + file.getName();
                File srgs = new File(file, "srgs");
                versions.add(new MappingVersion(version, srgs));
            }
        }

        versions.sort(null);
        return versions;
    }

    private static boolean hasAdditionalMappings(File file) {
        return new File(file, "snapshot").exists() || new File(file, "snapshot_nodoc").exists() || new File(file, "stable").exists() || new File(file, "stable_nodoc").exists();
    }

    private static List<MappingVersion> addFG1Mappings(File rootDir, String channel, List<MappingVersion> additionalMappings) {
        File mappingDir = new File(rootDir, channel);
        if(mappingDir.exists()) {
            for(String subdir : mappingDir.list()) {
                additionalMappings.add(new MappingVersion(rootDir.getName() + "-" + channel + "_" + subdir, new File(mappingDir, subdir + File.separator + "srgs")));
            }
        }
        return additionalMappings;
    }
}
