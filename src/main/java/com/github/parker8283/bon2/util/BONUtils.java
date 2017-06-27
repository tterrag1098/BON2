package com.github.parker8283.bon2.util;

import java.io.File;
import java.io.FileFilter;
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
            for(File file : fg1_versionFolders) {
                String name = file.getName();
                if(!name.startsWith("1.6") && versionMatcher.reset(name).matches()) {
                    File conf = new File(file, "unpacked" + File.separator + "conf");
                    if (conf.exists() && conf.isDirectory()) {
                        versions.add(new MappingVersion(name, conf));
                    }
                }
            }
        }

        File[] fg2_mappingsFolders = BONFiles.OCEANLABS_MCP_FOLDER.listFiles();
        if(fg2_mappingsFolders != null) {
            for(File file : fg2_mappingsFolders) {
                String name = file.getName();
                if(name.startsWith("mcp_s")) {
                    for(File file1 : file.listFiles(File::isDirectory)) {
                        versions.add(new MappingVersion(getFullVersion(file1), file1));
                    }
                }
            }
        }

        versions.sort(null);
        return versions;
    }
    
    private static String getFullVersion(File mappingsfolder) {
        return VersionLookup.INSTANCE.getVersionFor(mappingsfolder.getName()) + "-" + mappingsfolder.getParentFile().getName().substring(4) + "_" + mappingsfolder.getName();
    }
}
