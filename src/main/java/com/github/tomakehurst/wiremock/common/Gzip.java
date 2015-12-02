package com.github.tomakehurst.wiremock.common;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.google.common.base.Charsets.UTF_8;

public class Gzip {

    public static byte[] unGzip(byte[] gzippedContent) {
        try {
            GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(gzippedContent));
            return ByteStreams.toByteArray(gzipInputStream);
        } catch (IOException e) {
            return throwUnchecked(e, byte[].class);
        }
    }

    public static String unGzipToString(byte[] gzippedContent) {
        return new String(unGzip(gzippedContent));
    }

    public static byte[] gzip(String plainContent) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(bytes);
            gzipOutputStream.write(plainContent.getBytes(UTF_8));
            gzipOutputStream.close();
            return bytes.toByteArray();
        } catch (IOException e) {
            return throwUnchecked(e, byte[].class);
        }
    }
}
