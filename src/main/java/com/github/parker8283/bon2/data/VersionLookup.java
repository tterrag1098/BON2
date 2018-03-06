package com.github.parker8283.bon2.data;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import com.github.parker8283.bon2.data.VersionJson.MappingsJson;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public enum VersionLookup {

    INSTANCE;

    private static final String VERSION_JSON = "http://export.mcpbot.bspk.rs/versions.json";
    private static final Gson GSON = new GsonBuilder().create();

    private VersionJson jsoncache;

    public String getVersionFor(String version) {
        if (jsoncache != null) {
            for (String s : jsoncache.getVersions()) {
                MappingsJson mappings = jsoncache.getMappings(s);
                if (mappings.hasSnapshot(version) || mappings.hasStable(version)) {
                    return s;
                }
            }
        }
        return null;
    }
    
    public VersionJson getVersions() {
        return jsoncache;
    }

    @SuppressWarnings("serial")
    public void refresh() throws IOException {        
        URL url = new URL(VERSION_JSON);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();

        Reader in = new InputStreamReader(request.getInputStream());

        try {
            INSTANCE.jsoncache = new VersionJson(GSON.fromJson(in, new TypeToken<Map<String, MappingsJson>>() {}.getType()));
        } finally {
            in.close();
        }
    }
}
