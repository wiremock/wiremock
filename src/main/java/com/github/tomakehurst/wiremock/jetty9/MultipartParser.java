package com.github.tomakehurst.wiremock.jetty9;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.servlet.WireMockHttpServletMultipartAdapter;
import com.google.common.base.Function;
import org.eclipse.jetty.util.MultiPartInputStreamParser;

import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.google.common.collect.FluentIterable.from;

public class MultipartParser {

    @SuppressWarnings("unchecked")
    public static Collection<Request.Part> parse(byte[] body, String contentType) {
        MultiPartInputStreamParser parser = new MultiPartInputStreamParser(new ByteArrayInputStream(body), contentType, null, null);
        try {
            return from(parser.getParts()).transform(new Function<Part, Request.Part>() {
                @Override
                public Request.Part apply(Part input) {
                    return WireMockHttpServletMultipartAdapter.from(input);
                }
            }).toList();
        } catch (IOException | ServletException e) {
            return throwUnchecked(e, Collection.class);
        }
    }
}
