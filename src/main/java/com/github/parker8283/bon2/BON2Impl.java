package com.github.parker8283.bon2;

import java.io.File;
import java.io.IOException;

import com.github.parker8283.bon2.data.IErrorHandler;
import com.github.parker8283.bon2.data.IProgressListener;
import com.github.parker8283.bon2.data.MappingVersion;
import com.github.parker8283.bon2.srg.ClassCollection;
import com.github.parker8283.bon2.srg.Repo;
import com.github.parker8283.bon2.util.JarUtils;
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
    public static void remap(File inputJar, File outputJar, MappingVersion mappings, IErrorHandler errorHandler, IProgressListener progressListener) throws IOException {
        File srgsFolder = mappings.getSrgs();
        Repo.loadMappings(srgsFolder, progressListener);
        ClassCollection inputCC = JarUtils.readFromJar(inputJar, errorHandler, progressListener);
        ClassCollection outputCC = Remapper.remap(inputCC, progressListener);
        JarUtils.writeToJar(outputCC, outputJar, progressListener);
        progressListener.start(1, "Done!");
        progressListener.setProgress(1);
    }

    /**
     * Utility to help build a valid mapping version to pass into the remap process.
     * @param mcVer The Minecraft Version to use.
     * @param forgeVer The Forge Version to use.
     * @param useShippedMappings Use the FML-shipped mappings? (1.7.10 or earlier only)
     * @param mappingVer The mappings version to use. Can safely be null if useShippedMappings is true.
     * @return The formatted mappings version.
     */
    public static String buildMappingVer(String mcVer, String forgeVer, boolean useShippedMappings, String mappingVer) {
        return String.format("%s-%s-%s", mcVer, forgeVer, useShippedMappings ? "shipped" : mappingVer);
    }
}
