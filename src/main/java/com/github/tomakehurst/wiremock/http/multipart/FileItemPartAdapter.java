package com.github.tomakehurst.wiremock.http.multipart;

import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemHeaders;

import java.util.Iterator;

public class FileItemPartAdapter implements Request.Part {

    private final FileItem fileItem;

    public FileItemPartAdapter(FileItem fileItem) {
        this.fileItem = fileItem;
    }

    @Override
    public String getName() {
        return fileItem.getFieldName();
    }

    @Override
    public HttpHeader getHeader(String name) {
        Iterator<String> headerValues = fileItem.getHeaders().getHeaders(name);
        return new HttpHeader(name, Iterators.toArray(headerValues, String.class));
    }

    @Override
    public HttpHeaders getHeaders() {
        FileItemHeaders headers = fileItem.getHeaders();
        Iterator<String> i = headers.getHeaderNames();
        ImmutableList.Builder<HttpHeader> builder = ImmutableList.builder();
        while (i.hasNext()) {
            String name = i.next();
            builder.add(getHeader(name));
        }

        return new HttpHeaders(builder.build());
    }

    @Override
    public Body getBody() {
        return new Body(fileItem.get());
    }

    public static final Function<FileItem, Request.Part> TO_PARTS = new Function<FileItem, Request.Part>() {
        @Override
        public Request.Part apply(FileItem fileItem) {
            return new FileItemPartAdapter(fileItem);
        }
    };
}
