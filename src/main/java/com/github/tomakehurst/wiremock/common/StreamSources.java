package com.github.tomakehurst.wiremock.common;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;

public class StreamSources {
    private StreamSources() {
    }

    public static InputStreamSource forString(final String string, final Charset charset) {
        return new InputStreamSource() {
            @Override
            public InputStream getStream() {
                return string == null ? null : new ByteArrayInputStream(Strings.bytesFromString(string, charset));
            }
        };
    }

    public static InputStreamSource forBytes(final byte[] bytes) {
        return new InputStreamSource() {
            @Override
            public InputStream getStream() {
                return bytes == null ? null : new ByteArrayInputStream(bytes);
            }
        };
    }

    public static InputStreamSource forURI(final URI uri) {
        return  new InputStreamSource() {
            @Override
            public InputStream getStream() {
                try {
                    return uri == null ? null : new BufferedInputStream(uri.toURL().openStream());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

}
