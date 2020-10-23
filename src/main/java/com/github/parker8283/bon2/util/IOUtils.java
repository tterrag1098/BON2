package com.github.parker8283.bon2.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.github.parker8283.bon2.data.IProgressListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class IOUtils {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static byte[] readStreamFully(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.max(8192, is.available()));
        byte[] buffer = new byte[8192];
        int read;
        while((read = is.read(buffer)) >= 0) {
            baos.write(buffer, 0, read);
        }
        return baos.toByteArray();
    }

    public static int copy(InputStream in, OutputStream out) throws IOException {
        return copy(in, out, null);
    }

    public static int copy(InputStream in, OutputStream out, IProgressListener listener) throws IOException {
        if (listener != null)
            listener.setProgress(0);
        int count = 0;
        int c = 0;
        byte[] buf = new byte[0x100];
        while ((c = in.read(buf, 0, buf.length)) != -1) {
            out.write(buf, 0, c);
            count += c;
            if (listener != null)
                listener.setProgress(count);
        }
        return count;
    }

    public static <T> T loadJson(File target, Class<T> clz) throws IOException {
        try (InputStream in = new FileInputStream(target)) {
            return GSON.fromJson(new InputStreamReader(in), clz);
        }
    }
    public static <T> T loadJson(File target, Type clz) throws IOException {
        try (InputStream in = new FileInputStream(target)) {
            return GSON.fromJson(new InputStreamReader(in), clz);
        }
    }
    public static <T> T loadJson(InputStream in, Class<T> clz) throws IOException {
        return GSON.fromJson(new InputStreamReader(in), clz);
    }
    public static <T> T loadJson(byte[] in, Class<T> clz) throws IOException {
        return GSON.fromJson(new InputStreamReader(new ByteArrayInputStream(in)), clz);
    }

    public static byte[] getZipData(File file, String name) throws IOException {
        try (ZipFile zip = new ZipFile(file)) {
            ZipEntry entry = zip.getEntry(name);
            if (entry == null)
                throw new IOException("Zip Missing Entry: " + name + " File: " + file);

            return readStreamFully(zip.getInputStream(entry));
        }
    }
}
