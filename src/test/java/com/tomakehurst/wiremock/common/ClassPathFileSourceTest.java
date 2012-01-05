package com.tomakehurst.wiremock.common;

import static com.tomakehurst.wiremock.testsupport.WireMatchers.fileNamed;
import static com.tomakehurst.wiremock.testsupport.WireMatchers.hasExactlyIgnoringOrder;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ClassPathFileSourceTest {
    
    private ClassPathFileSource fileSource;
    
    @Before
    public void init() {
        fileSource = new ClassPathFileSource("filesource");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void listsTextFilesAtTopLevelIgnoringDirectories() {
        List<TextFile> files = fileSource.listFiles();
        
        assertThat(files, hasExactlyIgnoringOrder(
                fileNamed("one"), fileNamed("two"), fileNamed("three")));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void listsTextFilesRecursively() {
        List<TextFile> files = fileSource.listFilesRecursively();
        
        assertThat(files, hasExactlyIgnoringOrder(
                fileNamed("one"), fileNamed("two"), fileNamed("three"), 
                fileNamed("four"), fileNamed("five"), fileNamed("six"), 
                fileNamed("seven"), fileNamed("eight")));
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void throwsUnsupportedExceptionWhenAttemptingToWrite() {
        fileSource.writeTextFile("filename", "filecontents");
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void throwsUnsupportedExceptionWhenAttemptingToCreate() {
        fileSource.createIfNecessary();
    }
}
