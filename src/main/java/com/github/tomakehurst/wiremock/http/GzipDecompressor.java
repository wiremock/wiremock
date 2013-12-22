package com.github.tomakehurst.wiremock.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;

import static com.google.common.base.Charsets.UTF_8;

public class GzipDecompressor {
    /**
     * Decompress the binary gzipped content into a String.
     *
     * @param content the gzipped content to decompress
     * @return decompressed String.
     */
    public String decompressToUtf8String(byte[] content) {
        return new String(decompress(content), Charset.forName(UTF_8.name()));
    }

    /**
     * Decompress the binary gzipped content.
     *
     * @param content the gzipped content to decompress
     * @return decompressed bytes.
     */
    public byte[] decompress(byte[] content) {
        GZIPInputStream gin = null;
        try {
            gin = new GZIPInputStream(new ByteArrayInputStream(content));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] buf = new byte[8192];

            int read;
            while ((read = gin.read(buf)) != -1) {
                baos.write(buf,0,read);
            }

            return baos.toByteArray();

        } catch (IOException e) {
            return null;
        } finally {
            if (gin != null) {
                try {
                    gin.close();
                } catch (IOException e) {

                }
            }
        }
    }
}
