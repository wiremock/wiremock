package com.github.tomakehurst.wiremock.http.content;

import java.nio.charset.Charset;

public abstract class Content<T> {

    public enum Type {
        JSON, XML, PLAIN
    }

    protected final byte[] data;

    protected Content(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public abstract T getValue();
    public abstract boolean isBinary();
    public abstract String getAsString();

    public static Content fromBytes(byte[] bytes) {
        return new BinaryContent(bytes);
    }

    public static Content fromBase64(String base64) {
        return new BinaryContent(base64);
    }

    public static Content fromString(String s) {
        return new Text(s);
    }

    public static Content fromString(String s, Charset charset) {
        return new Text(s, charset);
    }

    public static Content fromString(String s, Type contentType) {
        return new Text(s, contentType);
    }
}
