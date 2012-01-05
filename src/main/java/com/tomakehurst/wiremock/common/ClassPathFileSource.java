package com.tomakehurst.wiremock.common;

import static java.lang.Thread.currentThread;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class ClassPathFileSource extends AbstractFileSource {
    
    private final String rootPath;
    
    public ClassPathFileSource(String rootPath) {
        super(getRootFile(rootPath));
        this.rootPath = rootPath;
    }
    
    private static File getRootFile(String rootPath) {
        URL rootUrl = currentThread().getContextClassLoader().getResource(rootPath);
        try {
            return new File(rootUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    

    @Override
    public FileSource child(String subDirectoryName) {
        return new ClassPathFileSource(rootPath + '/' + subDirectoryName);
    }

    @Override
    protected boolean readOnly() {
        return true;
    }

   

}
