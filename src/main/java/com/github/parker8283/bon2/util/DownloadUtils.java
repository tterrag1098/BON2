package com.github.parker8283.bon2.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.function.Consumer;

import com.github.parker8283.bon2.data.IProgressListener;

//Most of this code is just stolen straight from ForgeGradle, I am just to lazy to re-write it.
//It's so simple, I give it freely. -Lex
public class DownloadUtils {
    private static final int CACHE_TIMEOUT = 1000 * 60 * 60 * 1; //1 hour, so we don't spam the server

    private static HttpURLConnection connectHttpWithRedirects(URL url) throws IOException {
        return connectHttpWithRedirects(url, (setupCon) -> {});
    }

    private static HttpURLConnection connectHttpWithRedirects(URL url, Consumer<HttpURLConnection> setup) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setInstanceFollowRedirects(true);
        setup.accept(con);
        con.connect();
        if ("http".equalsIgnoreCase(url.getProtocol())) {
            int responseCode = con.getResponseCode();
            switch (responseCode) {
                case HttpURLConnection.HTTP_MOVED_TEMP:
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_SEE_OTHER:
                    String newLocation = con.getHeaderField("Location");
                    URL newUrl = new URL(newLocation);
                    if ("https".equalsIgnoreCase(newUrl.getProtocol())) {
                        // Escalate from http to https.
                        // This is not done automatically by HttpURLConnection.setInstanceFollowRedirects
                        // See https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4959149
                        return connectHttpWithRedirects(newUrl, setup);
                    }
                    break;
            }
        }
        return con;
    }

    public static boolean downloadEtag(URL url, File outputIn, boolean offline) throws IOException {
        return downloadEtag(url, outputIn, offline, null);
    }
    public static boolean downloadEtag(URL url, File outputIn, boolean offline, IProgressListener listener) throws IOException {
        final File output = outputIn.getAbsoluteFile();
        if (output.exists() && output.lastModified() > System.currentTimeMillis() - CACHE_TIMEOUT) {
            return true;
        }
        if (output.exists() && offline) {
            return true; //Use offline
        }
        File efile = new File(output.getAbsolutePath() + ".etag");
        String etag = "";
        if (efile.exists())
            etag = new String(Files.readAllBytes(efile.toPath()), StandardCharsets.UTF_8);

        final String initialEtagValue = etag;
        HttpURLConnection con = connectHttpWithRedirects(url, (setupCon) -> {
            if (output.exists())
                setupCon.setIfModifiedSince(output.lastModified());
            if (!initialEtagValue.isEmpty())
                setupCon.setRequestProperty("If-None-Match", initialEtagValue);
        });

        if (con.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
            output.setLastModified(new Date().getTime());
            return true;
        } else if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try {
                InputStream stream = con.getInputStream();
                int len = con.getContentLength();
                int read = -1;

                if (listener != null)
                    listener.setMax(len);

                output.getParentFile().mkdirs();
                try (FileOutputStream out = new FileOutputStream(output)) {
                    read = IOUtils.copy(stream, out, listener);
                }

                if (read != len) {
                    output.delete();
                    throw new IOException("Failed to read all of data from " + url + " got " + read + " expected " + len);
                }

                etag = con.getHeaderField("ETag");
                if (etag == null || etag.isEmpty())
                    Files.write(efile.toPath(), new byte[0]);
                else
                    Files.write(efile.toPath(), etag.getBytes(StandardCharsets.UTF_8));
                return true;
            } catch (IOException e) {
                output.delete();
                throw e;
            }
        }
        return false;
    }

    public static String downloadString(URL url) throws IOException {
        String proto = url.getProtocol().toLowerCase();

        if ("http".equals(proto) || "https".equals(proto)) {
            HttpURLConnection con = connectHttpWithRedirects(url);
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return downloadString(con);
            }
        } else {
            URLConnection con = url.openConnection();
            con.connect();
            return downloadString(con);
        }
        return null;
    }

    private static String downloadString(URLConnection con) throws IOException {
        InputStream stream = con.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len = con.getContentLength();
        int read = IOUtils.copy(stream, out);
        if (read != len)
            throw new IOException("Failed to read all of data from " + con.getURL() + " got " + read + " expected " + len);
        return new String(out.toByteArray(), StandardCharsets.UTF_8); //Read encoding from header?
    }

    public static boolean downloadFile(URL url, File output, boolean deleteOn404) {
        return downloadFile(url, output, deleteOn404, null);
    }
    public static boolean downloadFile(URL url, File output, boolean deleteOn404, IProgressListener listener) {
        String proto = url.getProtocol().toLowerCase();

        try {
            if ("http".equals(proto) || "https".equals(proto)) {
                HttpURLConnection con = connectHttpWithRedirects(url);
                int responseCode = con.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return downloadFile(con, output, listener);
                } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND && deleteOn404 && output.exists()) {
                    output.delete();
                }
            } else {
                URLConnection con = url.openConnection();
                con.connect();
                return downloadFile(con, output, listener);
            }
        } catch (FileNotFoundException e) {
            if (deleteOn404 && output.exists())
                output.delete();
        } catch (IOException e) {
            //Invalid URLs/File paths will cause FileNotFound or 404 errors.
            //As well as any errors during download.
            //So delete the output if it exists as it's invalid, and return false
            if (output.exists())
                output.delete();
            e.printStackTrace();
        }

        return false;
    }

    private static boolean downloadFile(URLConnection con, File output, IProgressListener listener) throws IOException {
        try {
            InputStream stream = con.getInputStream();
            int len = con.getContentLength();
            int read = -1;

            if (listener != null) {
                String tmp = con.getURL().toString();
                tmp = tmp.substring(tmp.lastIndexOf('/') + 1, tmp.length());
                listener.start(len, "Downloading: " + tmp);
            }

            output.getParentFile().mkdirs();

            try (FileOutputStream out = new FileOutputStream(output)) {
                read = IOUtils.copy(stream, out, listener);
            }

            if (read != len && len != -1) {
                output.delete();
                throw new IOException("Failed to read all of data from " + con.getURL() + " got " + read + " expected " + len);
            }

            return true;
        } catch (IOException e) {
            output.delete();
            throw e;
        }
    }

    public static boolean downloadWithCache(URL url, File target, boolean changing, boolean bypassLocal) throws IOException {
        return downloadWithCache(url, target, changing, bypassLocal, null);
    }

    public static boolean downloadWithCache(URL url, File target, boolean changing, boolean bypassLocal, IProgressListener listener) throws IOException {
        File md5_file = new File(target.getAbsolutePath() + ".md5");
        String actual = target.exists() ? HashFunction.MD5.hash(target) : null;

        if (target.exists() && !(changing || bypassLocal)) {
            String expected = md5_file.exists() ? new String(Files.readAllBytes(md5_file.toPath()), StandardCharsets.UTF_8) : null;
            if (expected == null || expected.equals(actual))
                return true;
            target.delete();
        }

        String expected = null;
        try {
            expected = downloadString(new URL(url.toString() + ".md5"));
        } catch (IOException e) {
            //Eat it, some repos don't have a simple checksum.
        }
        if (expected == null && bypassLocal) return false; // Ignore local file if the remote doesn't have it.
        if (expected == null && target.exists()) return true; //Assume we're good cuz they didn't have a MD5 on the server.
        if (expected != null && expected.equals(actual)) return true;

        if (target.exists())
            target.delete(); //Invalid checksum, delete and grab new

        if (!downloadFile(url, target, false, listener)) {
            target.delete();
            return false;
        }

        updateHash(target, HashFunction.MD5);
        return true;
    }

    private static void updateHash(File target, HashFunction... functions) throws IOException {
        for (HashFunction function : functions) {
            File cache = new File(target.getAbsolutePath() + "." + function.getExtension());
            if (target.exists()) {
                String hash = function.hash(target);
                Files.write(cache.toPath(), hash.getBytes());
            } else if (cache.exists()) {
                cache.delete();
            }
        }
    }
}
