package com.github.parker8283.bon2;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.github.parker8283.bon2.data.BONFiles;
import com.github.parker8283.bon2.data.IErrorHandler;
import com.github.parker8283.bon2.data.IProgressListener;
import com.github.parker8283.bon2.srg.ClassCollection;
import com.github.parker8283.bon2.util.IOUtils;
import com.github.parker8283.bon2.util.JarUtils;
import com.github.parker8283.bon2.util.MCPVersions.MCPVersion;
import com.github.parker8283.bon2.util.MappingVersions.MappingVersion;
import com.github.parker8283.bon2.util.Remapper;

public class BON2Impl {
    /**
     * Deobfuscates the inputJar to MCP names using the passed-in mappings.
     * @param inputJar Jar mapped to SRG names to be deobfuscated.
     * @param outputJar The file that will be the remapped jar.
     * @param mappings The mappings to use. In form "minecraftVer-forgeVer-mappingVer".
     * @param errorHandler An IErrorHandler impl to handle when an error is encountered in the remapping process.
     * @param progressListener An IProgressListener impl to handle listening to the progress of the remapping.
     */
    public static void remap(File inputJar, File outputJar, MCPVersion mcp, MappingVersion mappings, IErrorHandler errorHandler, IProgressListener progressListener) throws IOException {
        Map<String, String> mcpToMaped = loadMappings(mappings.getTarget(BONFiles.FG3_DOWNLOAD_CACHE), progressListener);
        ClassCollection inputCC = JarUtils.readFromJar(inputJar, errorHandler, progressListener);
        ClassCollection outputCC = Remapper.remap(mcpToMaped, inputCC, progressListener);
        JarUtils.writeToJar(outputCC, outputJar, progressListener);
        progressListener.start(1, "Done!");
        progressListener.setProgress(1);
    }


    private static Map<String, String> loadMappings(File mappings, IProgressListener progress) throws IOException {
        Map<String, String> ret = new HashMap<>();
        loadMappings(ret, mappings, "fields.csv", progress);
        loadMappings(ret, mappings, "methods.csv", progress);
        return ret;
    }

    private static void loadMappings(Map<String, String> map, File zip, String entry, IProgressListener progress) throws IOException {
        String[] lines = new String(IOUtils.getZipData(zip, entry), StandardCharsets.UTF_8).split("\\r?\\n");
        progress.start(lines.length - 1, "Reading in mappings: " + entry);
        for (int x = 1; x < lines.length; x++) {
            String[] values = lines[x].split(",");
            map.put(values[0], values[1]);
            progress.setProgress(x);
        }
    }
}
