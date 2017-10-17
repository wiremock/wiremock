package com.github.tomakehurst.wiremock.http.content;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.Xml;

import java.nio.charset.Charset;

import static com.github.tomakehurst.wiremock.common.Strings.bytesFromString;
import static com.github.tomakehurst.wiremock.common.Strings.stringFromBytes;
import static com.google.common.base.Charsets.UTF_8;

public class Text extends Content<String> {

    private final Charset charset;
    private final Type contentType;

    public Text(String s) {
        this(s, UTF_8);
    }

    public Text(String s, Type contentType) {
        this(s, UTF_8, contentType);
    }

    public Text(String s, Charset charset) {
        this(s, charset, null);
    }

    public Text(String s, Charset charset, Type contentType) {
        super(bytesFromString(s));
        this.charset = charset;
        this.contentType = contentType;
    }

    @Override
    public String getValue() {
        return getAsString();
    }

    @Override
    public boolean isBinary() {
        return false;
    }

    @Override
    public String getAsString() {
        String raw = stringFromBytes(data, charset);
        switch (getContentType()) {
            case JSON:
                return Json.prettyPrint(raw);
            case XML:
                return Xml.prettyPrint(raw);
            default:
                return Json.prettyPrint(raw);
        }
    }

    public Charset getCharset() {
        return charset;
    }

    public Type getContentType() {
        return contentType;
    }
}
