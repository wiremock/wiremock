/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.common;

import org.junit.Before;
import org.junit.Test;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.fileNamed;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.hasExactlyIgnoringOrder;
import static org.junit.Assert.assertThat;

public class ServletContextFileSourceTest {
    
    private ServletContextFileSource fileSource;
    
    @Before
    public void init() {
        fileSource = new ServletContextFileSource(new MockServletContext(), "filesource");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void listsTextFilesRecursively() {
        List<TextFile> files = fileSource.listFilesRecursively();
        
        assertThat(files, hasExactlyIgnoringOrder(
                fileNamed("one"), fileNamed("two"), fileNamed("three"), 
                fileNamed("four"), fileNamed("five"), fileNamed("six"), 
                fileNamed("seven"), fileNamed("eight"), fileNamed("deepfile.json")));
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void throwsUnsupportedExceptionWhenAttemptingToWrite() {
        fileSource.writeTextFile("filename", "filecontents");
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void throwsUnsupportedExceptionWhenAttemptingToCreate() {
        fileSource.createIfNecessary();
    }
    
    
    private static class MockServletContext implements ServletContext {

        @Override
        public ServletContext getContext(String uripath) {
            return null;
        }

        @Override
        public int getMajorVersion() {
            return 0;
        }

        @Override
        public int getMinorVersion() {
            return 0;
        }

        @Override
        public String getMimeType(String file) {
            return null;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Set getResourcePaths(String path) {
            return null;
        }

        @Override
        public URL getResource(String path) throws MalformedURLException {
            return null;
        }

        @Override
        public InputStream getResourceAsStream(String path) {
            return null;
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String path) {
            return null;
        }

        @Override
        public RequestDispatcher getNamedDispatcher(String name) {
            return null;
        }

        @Override
        public Servlet getServlet(String name) throws ServletException {
            return null;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Enumeration getServlets() {
            return null;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Enumeration getServletNames() {
            return null;
        }

        @Override
        public void log(String msg) {
        }

        @Override
        public void log(Exception exception, String msg) {
            
        }

        @Override
        public void log(String message, Throwable throwable) {
        }

        @Override
        public String getRealPath(String path) {
            return "src/test/resources/filesource";
        }

        @Override
        public String getServerInfo() {
            return null;
        }

        @Override
        public String getInitParameter(String name) {
            return null;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Enumeration getInitParameterNames() {
            return null;
        }

        @Override
        public Object getAttribute(String name) {
            return null;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Enumeration getAttributeNames() {
            return null;
        }

        @Override
        public void setAttribute(String name, Object object) {
        }

        @Override
        public void removeAttribute(String name) {
        }

        @Override
        public String getServletContextName() {
            return null;
        }

        @Override
        public String getContextPath() {
            // TODO Auto-generated method stub
            return null;
        }
        
    }
}
