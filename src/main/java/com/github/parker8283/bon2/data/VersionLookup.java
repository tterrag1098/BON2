package com.github.parker8283.bon2.data;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import com.github.parker8283.bon2.data.VersionJson.MappingsJson;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import gnu.trove.set.hash.TIntHashSet;

public enum VersionLookup {

    INSTANCE;

    private static final String VERSION_JSON = "http://export.mcpbot.bspk.rs/versions.json";
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(TIntHashSet.class, new JsonDeserializer<TIntHashSet>() {

        @Override
        public TIntHashSet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonArray()) {
                TIntHashSet ret = new TIntHashSet();
                JsonArray versions = json.getAsJsonArray();
                for (int i = 0; i < versions.size(); i++) {
                    ret.add(versions.get(i).getAsInt());
                }
                return ret;
            }
            throw new JsonParseException("Could not parse TIntHashSet, was not array.");
        }
    }).create();

    private VersionJson jsoncache = new VersionJson(Maps.newHashMap());

    public String getVersionFor(String version) {
        for (String s : jsoncache.getVersions()) {
            MappingsJson mappings = jsoncache.getMappings(s);
            if (mappings.hasSnapshot(version) || mappings.hasStable(version)) {
                return s;
            }
        }
        return "unknown";
    }

    @SuppressWarnings("serial")
    public void refresh() {

        try {
            URL url = new URL(VERSION_JSON);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();

            Reader in = new InputStreamReader(request.getInputStream());

            try {
                INSTANCE.jsoncache = new VersionJson(GSON.fromJson(in, new TypeToken<Map<String, MappingsJson>>(){}.getType()));
            } finally {
                in.close();
            }
        } catch (IOException e) {
            Throwables.propagate(e);
        }
    }
}
