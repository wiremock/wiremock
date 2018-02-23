package com.github.tomakehurst.wiremock.common;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;

public class StreamSources {
    private StreamSources() {
    }

    public static StreamSource forString(final String string, final Charset charset) {
        return new StreamSource() {
            @Override
            public InputStream getStream() {
                return string == null ? null : new ByteArrayInputStream(Strings.bytesFromString(string, charset));
            }
        };
    }

    public static StreamSource forBytes(final byte[] bytes) {
        return new StreamSource() {
            @Override
            public InputStream getStream() {
                return bytes == null ? null : new ByteArrayInputStream(bytes);
            }
        };
    }

    public static StreamSource forURI(final URI uri) {
        return  new StreamSource() {
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
