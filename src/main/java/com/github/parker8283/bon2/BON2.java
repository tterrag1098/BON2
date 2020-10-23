package com.github.parker8283.bon2;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.UIManager;

import com.github.parker8283.bon2.cli.CLIErrorHandler;
import com.github.parker8283.bon2.cli.CLIProgressListener;
import com.github.parker8283.bon2.data.BONFiles;
import com.github.parker8283.bon2.data.IErrorHandler;
import com.github.parker8283.bon2.data.IProgressListener;
import com.github.parker8283.bon2.util.BONUtils;
import com.github.parker8283.bon2.util.DownloadUtils;
import com.github.parker8283.bon2.util.MCPVersions;
import com.github.parker8283.bon2.util.MCPVersions.MCPVersion;
import com.github.parker8283.bon2.util.MappingVersions;
import com.github.parker8283.bon2.util.MinecraftVersions;
import com.github.parker8283.bon2.util.MappingVersions.MappingVersion;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecraftforge.srgutils.MinecraftVersion;

public class BON2 {
    public static final String VERSION = "Bearded Octo Nemesis v${DEV} by Parker8283. BON v1 by immibis.";

    public static void main(String[] args) throws Exception {
        if(args.length > 0) {
            parseArgs(args);
        } else {
            launchGui();
        }
    }

    private static void parseArgs(String[] args) throws Exception {
        OptionParser parser = new OptionParser();
        parser.accepts("help", "Prints this help menu").forHelp();
        parser.accepts("version", "Prints the version string").forHelp();
        parser.accepts("inputJar", "The jar file to deobfuscate").withRequiredArg().required();
        parser.accepts("outputJar", "The location and name of the output jar. Defaults to same dir and appends \"-deobf\"").withRequiredArg();
        parser.accepts("mcVer", "Minecraft version number").withRequiredArg();
        parser.accepts("mappingsVer", "Mapping version, must be in the format channel_version. Example: stable_18-1.12.2 or snapshot_20191126-1.13, For convienance, the MC version can be excluded and --mcVer will be used.").withRequiredArg().required();
        parser.accepts("notch", "Enables the full Notch names to MCP names mapping.");

        try {
            OptionSet options = parser.parse(args);
            if(options.has("help")) {
                System.out.println(VERSION);
                parser.printHelpOn(System.out);
                System.exit(0);
            }
            if(options.has("version")) {
                System.out.println(VERSION);
                System.exit(0);
            }

            boolean notch = options.has("notch");
            String inputJar = (String)options.valueOf("inputJar");
            String outputJar = options.has("outputJar") ? (String)options.valueOf("outputJar") : inputJar.replace(".jar", "-deobf.jar");
            String mcVer = (String)options.valueOf("mcVer");
            String mappingsVer = (String)options.valueOf("mappingsVer");
            if (mappingsVer.indexOf('-') == -1)
                mappingsVer = mappingsVer + '-' + mcVer;

            if(!new File(inputJar).exists()) {
                System.err.println("The provided inputJar does not exist");
                new FileNotFoundException(inputJar).printStackTrace();
                System.exit(1);
            }

            MCPVersion mcp = MCPVersions.get(MinecraftVersion.from(mcVer));
            if (notch && mcp == null) {
                System.err.println("The provided Minecraft Version \"" + mcVer +"\" is invalid. MCP/MCPConfig not found.");
                System.exit(1);
            }
            MappingVersion mapping = MappingVersions.getFromString(mappingsVer);
            if (mapping == null) {
                System.err.println("The provided mappingsVer \"" + mappingsVer + "\" is invalid. Unknown format, must be a valid MCP channel.");
                System.exit(1);
            }

            IErrorHandler errorHandler = new CLIErrorHandler();

            log(VERSION);
            log("Input JAR:       " + inputJar);
            log("Output JAR:      " + outputJar);
            log("Notch:           " + notch);
            log("Minecraft:       " + mcp);
            log("Mappings:        " + mappingsVer);
            log("Gradle User Dir: " + BONFiles.GRADLE_CACHES_FOLDER);

            try {
                IProgressListener progress = new CLIProgressListener();

                Map<String, URL> urls = MinecraftVersions.getDownloadUrls(mcp.getMCVersion());
                Map<File, URL> files = new HashMap<>();
                files.put(mapping.getTarget(), BONUtils.toUrl(mapping.getUrl()));
                if (notch) {
                    files.put(mcp.getTarget(), BONUtils.toUrl(mcp.getUrl()));
                    if (urls.containsKey("client"))
                        files.put(new File(BONFiles.FG3_MC_CACHE, mcp.getMCVersion() + "/client.jar"), urls.get("client"));
                    if (urls.containsKey("server"))
                        files.put(new File(BONFiles.FG3_MC_CACHE, mcp.getMCVersion() + "/server.jar"), urls.get("server"));
                }

                boolean failed = false;
                for (Entry<File, URL> entry : files.entrySet()) {
                    File target = entry.getKey();
                    URL url = entry.getValue();
                    try {
                        log("Downloading " + target.getName());
                        if (!DownloadUtils.downloadWithCache(url, target, false, false, progress)) {
                            if (target.exists())
                                target.delete();
                            System.err.println("Failed to download: " + url + " to " + target.getAbsolutePath());
                            failed = true;
                        }
                    } catch (IOException ex) {
                        System.err.println("Failed to download: " + url + " to " + target.getAbsolutePath());
                        ex.printStackTrace();
                        if (target.exists())
                            target.delete();
                        failed = true;
                    }
                }

                if (failed)
                    System.exit(1);

                log("Download Complete");
                BON2Impl.remap(new File(inputJar), new File(outputJar), notch ? mcp : null, mapping, errorHandler, progress);
            } catch(Exception e) {
                logErr(e.getMessage(), e);
                System.exit(1);
            }
        } catch(OptionException e) {
            e.printStackTrace();
            parser.printHelpOn(System.err);
        }
    }

    private static void log(String message) {
        System.out.println(message);
    }

    private static void logErr(String message, Throwable t) {
        System.err.println(message);
        t.printStackTrace();
    }

    private static void launchGui() {
        log(VERSION);
        log("No arguments passed. Launching gui...");
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    BON2Gui frame = new BON2Gui();
                    frame.setVisible(true);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
