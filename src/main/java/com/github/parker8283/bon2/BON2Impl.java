package com.github.parker8283.bon2;

import java.io.File;

import com.github.parker8283.bon2.data.ILogHandler;

public class BON2Impl {

    /**
     * Deobfuscates
     * @param inputJar Jar mapped to SRG names to be deobfuscated.
     * @param outputJar The file that will be the remapped jar.
     * @param mappings The mappings to use. In form "minecraftVer-forgeVer-mappingVer".
     * @param logHandler An ILogHandler impl to handle logging messages from the remapping process.
     * @param thread true if we should thread the process.
     */
    public static void remap(File inputJar, File outputJar, String mappings, ILogHandler logHandler, boolean thread) {
        //TODO Move process into here
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
