package com.github.tomakehurst.wiremock.http.content;

import com.google.common.io.BaseEncoding;

public class BinaryContent extends Content<byte[]> {

    public BinaryContent(byte[] data) {
        super(data);
    }

    @Override
    public byte[] getValue() {
        return getData();
    }

    public BinaryContent(String base64) {
        this(BaseEncoding.base64().decode(base64));
    }

    @Override
    public boolean isBinary() {
        return true;
    }

    @Override
    public String getAsString() {
        return BaseEncoding.base64().encode(data);
    }


}
