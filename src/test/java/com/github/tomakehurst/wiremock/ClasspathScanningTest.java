package com.github.tomakehurst.wiremock;

import com.google.common.io.Resources;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ClasspathScanningTest {

    @Test
    public void test() throws Exception {
        String path = "META-INF/maven/org.mortbay.jetty/jetty";
//        String path = "test-requests";
        URI dirInJarUri = Resources.getResource(path).toURI();
        System.out.println(dirInJarUri.getScheme());

        String jarFile = dirInJarUri.getSchemeSpecificPart().split("!")[0];
        File file = new File(URI.create(jarFile));

        List<String> results = new ArrayList<String>();
        ZipFile zf = new ZipFile(file);
        final Enumeration e = zf.entries();
        while(e.hasMoreElements()){
            final ZipEntry ze = (ZipEntry) e.nextElement();
            final String fileName = ze.getName();
            if (fileName.startsWith(path)) {
                results.add(fileName);
            }
        }

        System.out.println(results);
    }
}
