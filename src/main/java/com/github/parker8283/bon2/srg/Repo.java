package com.github.parker8283.bon2.srg;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.github.parker8283.bon2.data.IProgressListener;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

public class Repo {

    public static final Map<String, Mapping> repo = Maps.newHashMap();
    
    public static void loadMappings(File srgsDir, IProgressListener progress) throws IOException {
        loadMappings(new File(srgsDir, "fields.csv"), Mapping.Type.FIELD, progress);
        loadMappings(new File(srgsDir, "methods.csv"), Mapping.Type.METHOD, progress);
    }
    
    private static void loadMappings(File csvFile, Mapping.Type type, IProgressListener progress) throws IOException {
        List<String> lines = Files.readLines(csvFile, Charsets.UTF_8);
        lines.remove(0); // header line
        int linesRead = 0;
        progress.start(lines.size(), "Reading in mappings: " + csvFile.getName());
        for (String line : lines) {
            String[] values = line.split(",");
            repo.put(values[0], new Mapping(type, values[1], values[0]));
            progress.setProgress(++linesRead);
        }
    }
}
