package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class UrlEncodingHelper implements Helper<Object> {

    @Override
    public Object apply(Object context, Options options) throws IOException {
        Object encodingObj = options.hash.get("encoding");
        String encoding = encodingObj != null ? encodingObj.toString() : "utf-8";
        if (Boolean.TRUE.equals(options.hash.get("decode"))) {
            return decode(context.toString(), encoding);
        }

        return encode(context.toString(), encoding);
    }

    private String encode(String value, String encoding) throws IOException {
        try {
            return URLEncoder.encode(value, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new IOException(e);
        }
    }

    private String decode(String value, String encoding) throws IOException {
        try {
            return URLDecoder.decode(value, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new IOException(e);
        }
    }
}
