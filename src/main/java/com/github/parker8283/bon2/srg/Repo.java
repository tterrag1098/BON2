package com.github.parker8283.bon2.srg;

import java.io.*;
import java.util.Map;

import com.github.parker8283.bon2.data.IProgressListener;
import com.github.parker8283.bon2.util.IOUtils;
import com.google.common.collect.Maps;

public class Repo {
    public static final Map<String, Mapping> repo = Maps.newHashMap();

    public static void loadMappings(File srgsDir, IProgressListener progress) throws IOException {
        File mcpToSrg = new File(srgsDir, "mcp-srg.srg");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(mcpToSrg)));
        String line;
        int linesRead = 0;
        progress.start(21466, "Reading in mappings");
        while((line = br.readLine()) != null) {
            String type = line.substring(0, 2);
            Mapping.Type mappingType = Mapping.Type.getByCode(type);
            if(mappingType == Mapping.Type.PACKAGE || mappingType == Mapping.Type.CLASS) {
                continue;
            } else if(mappingType == Mapping.Type.METHOD) {
                String mcpline = line.substring(4, IOUtils.getSecondToLastIndexOf(line, ' '));
                String srgLine = line.substring(IOUtils.getSecondToLastIndexOf(line, ' ') + 1);
                String mcpNameNoSig = mcpline.substring(0, mcpline.indexOf(' '));
                String srgNameNoSig = srgLine.substring(0, srgLine.indexOf(' '));
                String mcpName = mcpNameNoSig.substring(mcpNameNoSig.lastIndexOf('/') + 1);
                String srgName = srgNameNoSig.substring(srgNameNoSig.lastIndexOf('/') + 1);
                repo.put(srgName, new Mapping(mappingType, mcpName, srgName));
            } else if(mappingType == Mapping.Type.FIELD) {
                String mcpLine = line.substring(4, line.lastIndexOf(' '));
                String srgLine = line.substring(line.lastIndexOf(' ') + 1);
                String mcpName = mcpLine.substring(mcpLine.lastIndexOf('/') + 1);
                String srgName = srgLine.substring(srgLine.lastIndexOf('/') + 1);
                repo.put(srgName, new Mapping(mappingType, mcpName, srgName));
            }
            progress.setProgress(++linesRead);
        }
        br.close();
    }
}
