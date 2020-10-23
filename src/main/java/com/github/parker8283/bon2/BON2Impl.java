package com.github.parker8283.bon2;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.commons.Remapper;

import com.github.parker8283.bon2.data.BONFiles;
import com.github.parker8283.bon2.data.IErrorHandler;
import com.github.parker8283.bon2.data.IProgressListener;
import com.github.parker8283.bon2.srg.ClassCollection;
import com.github.parker8283.bon2.srg.Inheritance;
import com.github.parker8283.bon2.srg.SimpleRemapper;
import com.github.parker8283.bon2.srg.SmartRemapper;
import com.github.parker8283.bon2.util.IOUtils;
import com.github.parker8283.bon2.util.JarUtils;
import com.github.parker8283.bon2.util.MCPVersions.MCPVersion;
import com.github.parker8283.bon2.util.MappingVersions.MappingVersion;

import net.minecraftforge.srgutils.IMappingFile;

public class BON2Impl {
    /**
     * Deobfuscates the inputJar to MCP names using the passed-in mappings.
     * @param inputJar Jar mapped to SRG names to be deobfuscated.
     * @param outputJar The file that will be the remapped jar.
     * @param mappings The mappings to use. In form "minecraftVer-forgeVer-mappingVer".
     * @param errorHandler An IErrorHandler impl to handle when an error is encountered in the remapping process.
     * @param progress An IProgressListener impl to handle listening to the progress of the remapping.
     */
    public static void remap(File inputJar, File outputJar, MCPVersion mcp, MappingVersion mappings, IErrorHandler errorHandler, IProgressListener progress) throws IOException {
        Map<String, String> mcpToMapped = loadMappings(mappings.getTarget(), progress);
        Remapper remapper = new SimpleRemapper(mcpToMapped);
        ClassCollection inputCC = JarUtils.readFromJar(inputJar, errorHandler, progress);
        if (mcp != null) {
            ClassCollection client = JarUtils.readFromJar(new File(BONFiles.FG3_MC_CACHE, mcp.getMCVersion() + "/client.jar"), errorHandler, progress, false);
            ClassCollection server = JarUtils.readFromJar(new File(BONFiles.FG3_MC_CACHE, mcp.getMCVersion() + "/server.jar"), errorHandler, progress, false);
            progress.start(client.getClasses().size() + server.getClasses().size() + inputCC.getClasses().size(), "Building inheritance");

            Inheritance inh = new Inheritance();
            inh.addTree(client, false, 0, progress);
            inh.addTree(server, true, client.getClasses().size(), progress);
            inh.addTree(inputCC, false, client.getClasses().size() + server.getClasses().size(), progress);
            inh.bake(progress);

            File mcpTarget = mcp.getTarget();
            if (!mcpTarget.exists())
                throw new IllegalStateException("Could not load MCP data, File missing: " + mcpTarget);
            String entry = mcp.getMappings(mcpTarget);
            if (entry == null)
                throw new IllegalStateException("Could not load MCP data, Missing entry name: " + mcpTarget);
            byte[] data = IOUtils.getZipData(mcpTarget, entry);
            if (data == null)
                throw new IllegalStateException("Could not load MCP data, Missing entry \"" + entry +"\" in " + mcpTarget);

            IMappingFile srg = IMappingFile.load(new ByteArrayInputStream(data));

            remapper = new SmartRemapper(mcpToMapped, inh, srg);
        }
        JarUtils.writeToJar(inputCC, outputJar, remapper, progress);
        progress.start(1, "Done!");
        progress.setProgress(1);
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
