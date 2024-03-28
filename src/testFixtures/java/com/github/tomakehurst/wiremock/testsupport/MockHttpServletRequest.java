/*
 * Copyright (C) 2012-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.testsupport;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

public class MockHttpServletRequest implements HttpServletRequest {

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
  public String getCharacterEncoding() {

    return null;
  }

  @Override
  public void setCharacterEncoding(String env) throws UnsupportedEncodingException {}

  @Override
  public int getContentLength() {

    return 0;
  }

  @Override
  public long getContentLengthLong() {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public String getContentType() {

    return null;
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {

    return null;
  }

  @Override
  public String getParameter(String name) {

    return null;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Enumeration getParameterNames() {

    return null;
  }

  @Override
  public String[] getParameterValues(String name) {

    return null;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Map getParameterMap() {

    return null;
  }

  @Override
  public String getProtocol() {

    return null;
  }

  @Override
  public String getScheme() {

    return null;
  }

  @Override
  public String getServerName() {

    return null;
  }

  @Override
  public int getServerPort() {

    return 0;
  }

  @Override
  public BufferedReader getReader() throws IOException {

    return null;
  }

  @Override
  public String getRemoteAddr() {

    return null;
  }

  @Override
  public String getRemoteHost() {

    return null;
  }

  @Override
  public void setAttribute(String name, Object o) {}

  @Override
  public void removeAttribute(String name) {}

  @Override
  public Locale getLocale() {

    return null;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Enumeration getLocales() {

    return null;
  }

  @Override
  public boolean isSecure() {

    return false;
  }

  @Override
  public RequestDispatcher getRequestDispatcher(String path) {

    return null;
  }

  @Override
  public String getRealPath(String path) {

    return null;
  }

  @Override
  public int getRemotePort() {

    return 0;
  }

  @Override
  public String getLocalName() {

    return null;
  }

  @Override
  public String getLocalAddr() {

    return null;
  }

  @Override
  public int getLocalPort() {

    return 0;
  }

  @Override
  public ServletContext getServletContext() {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public AsyncContext startAsync() throws IllegalStateException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
      throws IllegalStateException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public boolean isAsyncStarted() {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public boolean isAsyncSupported() {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public AsyncContext getAsyncContext() {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public DispatcherType getDispatcherType() {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public String getAuthType() {

    return null;
  }

  @Override
  public Cookie[] getCookies() {

    return null;
  }

  @Override
  public long getDateHeader(String name) {

    return 0;
  }

  @Override
  public String getHeader(String name) {

    return null;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Enumeration getHeaders(String name) {

    return null;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Enumeration getHeaderNames() {

    return null;
  }

  @Override
  public int getIntHeader(String name) {

    return 0;
  }

  @Override
  public String getMethod() {

    return null;
  }

  @Override
  public String getPathInfo() {

    return null;
  }

  @Override
  public String getPathTranslated() {

    return null;
  }

  @Override
  public String getContextPath() {

    return null;
  }

  @Override
  public String getQueryString() {

    return null;
  }

  @Override
  public String getRemoteUser() {

    return null;
  }

  @Override
  public boolean isUserInRole(String role) {

    return false;
  }

  @Override
  public Principal getUserPrincipal() {

    return null;
  }

  @Override
  public String getRequestedSessionId() {

    return null;
  }

  @Override
  public String getRequestURI() {

    return null;
  }

  @Override
  public StringBuffer getRequestURL() {

    return null;
  }

  @Override
  public String getServletPath() {

    return null;
  }

  @Override
  public HttpSession getSession(boolean create) {

    return null;
  }

  @Override
  public HttpSession getSession() {

    return null;
  }

  @Override
  public String changeSessionId() {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public boolean isRequestedSessionIdValid() {

    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromCookie() {

    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromURL() {

    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromUrl() {

    return false;
  }

  @Override
  public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public void login(String username, String password) throws ServletException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public void logout() throws ServletException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public Collection<Part> getParts() throws IOException, ServletException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public Part getPart(String name) throws IOException, ServletException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Override
  public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass)
      throws IOException, ServletException {
    throw new UnsupportedOperationException("not yet implemented");
  }
}
