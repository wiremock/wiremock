/*
 * Copyright (C) 2022-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http.multipart;

import static java.lang.String.format;

import com.github.tomakehurst.wiremock.common.Exceptions;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadBase.FileUploadIOException;
import org.apache.commons.fileupload.FileUploadBase.IOFileUploadException;
import org.apache.commons.fileupload.FileUploadBase.InvalidContentTypeException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.fileupload.ParameterParser;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.UploadContext;
import org.apache.commons.fileupload.util.Closeable;
import org.apache.commons.fileupload.util.FileItemHeadersImpl;
import org.apache.commons.fileupload.util.LimitedInputStream;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;

/**
 * The implementation is largely ported from {@link org.apache.commons.fileupload.FileUpload} and
 * {@link org.apache.commons.fileupload.FileUploadBase} to support 'javax.servlet' instead of
 * 'javax.servlet'. The standard support of multipart content type by Jetty in limited to
 * 'multipart/form-data', so 'multipart/mixed' and 'multipart/related' are not recognized and parsed
 * properly. To preserve backward compatibility and support wider range of multipart content,
 * re-implementing this part of the upload.
 */
class FileUpload {
  private final FileItemFactory fileItemFactory;

  FileUpload(FileItemFactory fileItemFactory) {
    this.fileItemFactory = fileItemFactory;
  }

  /**
   * The maximum size permitted for the complete request, as opposed to {@link #fileSizeMax}. A
   * value of -1 indicates no maximum.
   */
  private long sizeMax = -1;

  /**
   * The maximum size permitted for a single uploaded file, as opposed to {@link #sizeMax}. A value
   * of -1 indicates no maximum.
   */
  private long fileSizeMax = -1;

  /** The content encoding to use when reading part headers. */
  private String headerEncoding;

  protected FileItemIterator getItemIterator(RequestContext ctx)
      throws FileUploadException, IOException {
    try {
      return new FileItemIteratorImpl(ctx);
    } catch (FileUploadIOException e) {
      // unwrap encapsulated SizeException
      throw (FileUploadException) e.getCause();
    }
  }

  protected FileItemFactory getFileItemFactory() {
    return fileItemFactory;
  }

  /**
   * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a> compliant <code>
   * multipart/form-data</code> stream.
   *
   * @param ctx The context for the request to be parsed.
   * @return A list of <code>FileItem</code> instances parsed from the request, in the order that
   *     they were transmitted.
   * @throws FileUploadException if there are problems reading/parsing the request or storing files.
   */
  public List<FileItem> parseRequest(RequestContext ctx) throws FileUploadException {
    List<FileItem> items = new ArrayList<FileItem>();
    boolean successful = false;
    try {
      FileItemIterator iter = getItemIterator(ctx);
      FileItemFactory fac = getFileItemFactory();
      if (fac == null) {
        throw new NullPointerException("No FileItemFactory has been set.");
      }
      while (iter.hasNext()) {
        final FileItemStream item = iter.next();
        // Don't use getName() here to prevent an InvalidFileNameException.
        final String fileName = ((FileItemIteratorImpl.FileItemStreamImpl) item).name;
        FileItem fileItem =
            fac.createItem(
                item.getFieldName(), item.getContentType(), item.isFormField(), fileName);
        items.add(fileItem);
        try {
          Streams.copy(item.openStream(), fileItem.getOutputStream(), true);
        } catch (FileUploadIOException e) {
          throw (FileUploadException) e.getCause();
        } catch (IOException e) {
          throw new IOFileUploadException(
              format(
                  "Processing of %s request failed. %s",
                  FileUploadBase.MULTIPART_FORM_DATA, e.getMessage()),
              e);
        }
        final FileItemHeaders fih = item.getHeaders();
        fileItem.setHeaders(fih);
      }
      successful = true;
      return items;
    } catch (FileUploadIOException e) {
      throw (FileUploadException) e.getCause();
    } catch (IOException e) {
      throw new FileUploadException(e.getMessage(), e);
    } finally {
      if (!successful) {
        for (FileItem fileItem : items) {
          try {
            fileItem.delete();
          } catch (Exception ignored) {
            // ignored TODO perhaps add to tracker delete failure list somehow?
          }
        }
      }
    }
  }

  /**
   * Retrieves the boundary from the <code>Content-type</code> header.
   *
   * @param contentType The value of the content type header from which to extract the boundary
   *     value.
   * @return The boundary, as a byte array.
   */
  protected byte[] getBoundary(String contentType) {
    ParameterParser parser = new ParameterParser();
    parser.setLowerCaseNames(true);
    // Parameter parser can handle null input
    Map<String, String> params = parser.parse(contentType, new char[] {';', ','});
    String boundaryStr = params.get("boundary");

    if (boundaryStr == null) {
      return null;
    }
    byte[] boundary;
    try {
      boundary = boundaryStr.getBytes("ISO-8859-1");
    } catch (UnsupportedEncodingException e) {
      boundary = boundaryStr.getBytes(); // Intentionally falls back to default charset
    }
    return boundary;
  }

  /**
   * Retrieves the file name from the <code>Content-disposition</code> header.
   *
   * @param headers The HTTP headers object.
   * @return The file name for the current <code>encapsulation</code>.
   */
  protected String getFileName(FileItemHeaders headers) {
    return getFileName(headers.getHeader(FileUploadBase.CONTENT_DISPOSITION));
  }

  /**
   * Returns the given content-disposition headers file name.
   *
   * @param pContentDisposition The content-disposition headers value.
   * @return The file name
   */
  private String getFileName(String pContentDisposition) {
    String fileName = null;
    if (pContentDisposition != null) {
      String cdl = pContentDisposition.toLowerCase(Locale.ENGLISH);
      if (cdl.startsWith(FileUploadBase.FORM_DATA) || cdl.startsWith(FileUploadBase.ATTACHMENT)) {
        ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        // Parameter parser can handle null input
        Map<String, String> params = parser.parse(pContentDisposition, ';');
        if (params.containsKey("filename")) {
          fileName = params.get("filename");
          if (fileName != null) {
            fileName = fileName.trim();
          } else {
            // Even if there is no value, the parameter is present,
            // so we return an empty file name rather than no file
            // name.
            fileName = "";
          }
        }
      }
    }
    return fileName;
  }

  /**
   * Retrieves the field name from the <code>Content-disposition</code> header.
   *
   * @param headers A <code>Map</code> containing the HTTP request headers.
   * @return The field name for the current <code>encapsulation</code>.
   */
  protected String getFieldName(FileItemHeaders headers) {
    return getFieldName(headers.getHeader(FileUploadBase.CONTENT_DISPOSITION));
  }

  /**
   * Returns the field name, which is given by the content-disposition header.
   *
   * @param pContentDisposition The content-dispositions header value.
   * @return The field jake
   */
  private String getFieldName(String pContentDisposition) {
    String fieldName = null;
    if (pContentDisposition != null
        && pContentDisposition.toLowerCase(Locale.ENGLISH).startsWith(FileUploadBase.FORM_DATA)) {
      ParameterParser parser = new ParameterParser();
      parser.setLowerCaseNames(true);
      // Parameter parser can handle null input
      Map<String, String> params = parser.parse(pContentDisposition, ';');
      fieldName = params.get("name");
      if (fieldName != null) {
        fieldName = fieldName.trim();
      }
    }
    return fieldName;
  }

  /**
   * Parses the <code>header-part</code> and returns as key/value pairs.
   *
   * <p>If there are multiple headers of the same names, the name will map to a comma-separated list
   * containing the values.
   *
   * @param headerPart The <code>header-part</code> of the current <code>encapsulation</code>.
   * @return A <code>Map</code> containing the parsed HTTP request headers.
   */
  protected FileItemHeaders getParsedHeaders(String headerPart) {
    final int len = headerPart.length();
    FileItemHeadersImpl headers = newFileItemHeaders();
    int start = 0;
    for (; ; ) {
      int end = parseEndOfLine(headerPart, start);
      if (start == end) {
        break;
      }
      StringBuilder header = new StringBuilder(headerPart.substring(start, end));
      start = end + 2;
      while (start < len) {
        int nonWs = start;
        while (nonWs < len) {
          char c = headerPart.charAt(nonWs);
          if (c != ' ' && c != '\t') {
            break;
          }
          ++nonWs;
        }
        if (nonWs == start) {
          break;
        }
        // Continuation line found
        end = parseEndOfLine(headerPart, nonWs);
        header.append(" ").append(headerPart.substring(nonWs, end));
        start = end + 2;
      }
      parseHeaderLine(headers, header.toString());
    }
    return headers;
  }

  /**
   * Creates a new instance of {@link FileItemHeaders}.
   *
   * @return The new instance.
   */
  protected FileItemHeadersImpl newFileItemHeaders() {
    return new FileItemHeadersImpl();
  }

  /**
   * Skips bytes until the end of the current line.
   *
   * @param headerPart The headers, which are being parsed.
   * @param end Index of the last byte, which has yet been processed.
   * @return Index of the \r\n sequence, which indicates end of line.
   */
  private int parseEndOfLine(String headerPart, int end) {
    int index = end;
    for (; ; ) {
      int offset = headerPart.indexOf('\r', index);
      if (offset == -1 || offset + 1 >= headerPart.length()) {
        throw new IllegalStateException("Expected headers to be terminated by an empty line.");
      }
      if (headerPart.charAt(offset + 1) == '\n') {
        return offset;
      }
      index = offset + 1;
    }
  }

  /**
   * Reads the next header line.
   *
   * @param headers String with all headers.
   * @param header Map where to store the current header.
   */
  private void parseHeaderLine(FileItemHeadersImpl headers, String header) {
    final int colonOffset = header.indexOf(':');
    if (colonOffset == -1) {
      // This header line is malformed, skip it.
      return;
    }
    String headerName = header.substring(0, colonOffset).trim();
    String headerValue = header.substring(header.indexOf(':') + 1).trim();
    headers.addHeader(headerName, headerValue);
  }

  /** The iterator, which is returned by {@link FileUpload#getItemIterator(RequestContext)}. */
  private class FileItemIteratorImpl implements FileItemIterator {

    /** Default implementation of {@link FileItemStream}. */
    class FileItemStreamImpl implements FileItemStream {

      /** The file items content type. */
      private final String contentType;

      /** The file items field name. */
      private final String fieldName;

      /** The file items file name. */
      private final String name;

      /** Whether the file item is a form field. */
      private final boolean formField;

      /** The file items input stream. */
      private final InputStream stream;

      /** Whether the file item was already opened. */
      private boolean opened;

      /** The headers, if any. */
      private FileItemHeaders headers;

      /**
       * Creates a new instance.
       *
       * @param pName The items file name, or null.
       * @param pFieldName The items field name.
       * @param pContentType The items content type, or null.
       * @param pFormField Whether the item is a form field.
       * @param pContentLength The items content length, if known, or -1
       * @throws IOException Creating the file item failed.
       */
      FileItemStreamImpl(
          String pName,
          String pFieldName,
          String pContentType,
          boolean pFormField,
          long pContentLength)
          throws IOException {
        name = pName;
        fieldName = pFieldName;
        contentType = pContentType;
        formField = pFormField;
        if (fileSizeMax != -1) { // Check if limit is already exceeded
          if (pContentLength != -1 && pContentLength > fileSizeMax) {
            FileSizeLimitExceededException e =
                new FileSizeLimitExceededException(
                    format(
                        "The field %s exceeds its maximum permitted size of %s bytes.",
                        fieldName, Long.valueOf(fileSizeMax)),
                    pContentLength,
                    fileSizeMax);
            e.setFileName(pName);
            e.setFieldName(pFieldName);
            throw new FileUploadIOException(e);
          }
        }
        // OK to construct stream now
        final MultipartStream.ItemInputStream itemStream = newInputStream(multi);
        InputStream istream = itemStream;
        if (fileSizeMax != -1) {
          istream =
              new LimitedInputStream(istream, fileSizeMax) {
                @Override
                protected void raiseError(long pSizeMax, long pCount) throws IOException {
                  itemStream.close();
                  FileSizeLimitExceededException e =
                      new FileSizeLimitExceededException(
                          format(
                              "The field %s exceeds its maximum permitted size of %s bytes.",
                              fieldName, Long.valueOf(pSizeMax)),
                          pCount,
                          pSizeMax);
                  e.setFieldName(fieldName);
                  e.setFileName(name);
                  throw new FileUploadIOException(e);
                }
              };
        }
        stream = istream;
      }

      private MultipartStream.ItemInputStream newInputStream(MultipartStream multipartStream) {
        return Exceptions.uncheck(
            () -> {
              final Method newInputStreamMethod =
                  multipartStream.getClass().getDeclaredMethod("newInputStream");
              newInputStreamMethod.setAccessible(true);
              return (MultipartStream.ItemInputStream) newInputStreamMethod.invoke(multipartStream);
            },
            MultipartStream.ItemInputStream.class);
      }

      /**
       * Returns the items content type, or null.
       *
       * @return Content type, if known, or null.
       */
      @Override
      public String getContentType() {
        return contentType;
      }

      /**
       * Returns the items field name.
       *
       * @return Field name.
       */
      @Override
      public String getFieldName() {
        return fieldName;
      }

      /**
       * Returns the items file name.
       *
       * @return File name, if known, or null.
       * @throws InvalidFileNameException The file name contains a NUL character, which might be an
       *     indicator of a security attack. If you intend to use the file name anyways, catch the
       *     exception and use InvalidFileNameException#getName().
       */
      @Override
      public String getName() {
        return Streams.checkFileName(name);
      }

      /**
       * Returns, whether this is a form field.
       *
       * @return True, if the item is a form field, otherwise false.
       */
      @Override
      public boolean isFormField() {
        return formField;
      }

      /**
       * Returns an input stream, which may be used to read the items contents.
       *
       * @return Opened input stream.
       * @throws IOException An I/O error occurred.
       */
      @Override
      public InputStream openStream() throws IOException {
        if (opened) {
          throw new IllegalStateException("The stream was already opened.");
        }
        if (stream instanceof Closeable && ((Closeable) stream).isClosed()) {
          throw new FileItemStream.ItemSkippedException();
        }
        return stream;
      }

      /**
       * Closes the file item.
       *
       * @throws IOException An I/O error occurred.
       */
      void close() throws IOException {
        stream.close();
      }

      /**
       * Returns the file item headers.
       *
       * @return The items header object
       */
      @Override
      public FileItemHeaders getHeaders() {
        return headers;
      }

      /**
       * Sets the file item headers.
       *
       * @param pHeaders The items header object
       */
      @Override
      public void setHeaders(FileItemHeaders pHeaders) {
        headers = pHeaders;
      }
    }

    /** The multi part stream to process. */
    private final MultipartStream multi;

    /** The boundary, which separates the various parts. */
    private final byte[] boundary;

    /** The item, which we currently process. */
    private FileItemStreamImpl currentItem;

    /** The current items field name. */
    private String currentFieldName;

    /** Whether we are currently skipping the preamble. */
    private boolean skipPreamble;

    /** Whether the current item may still be read. */
    private boolean itemValid;

    /** Whether we have seen the end of the file. */
    private boolean eof;

    /**
     * Creates a new instance.
     *
     * @param ctx The request context.
     * @throws FileUploadException An error occurred while parsing the request.
     * @throws IOException An I/O error occurred.
     */
    FileItemIteratorImpl(RequestContext ctx) throws FileUploadException, IOException {
      if (ctx == null) {
        throw new NullPointerException("ctx parameter");
      }

      String contentType = ctx.getContentType();
      if ((null == contentType)
          || (!contentType.toLowerCase(Locale.ENGLISH).startsWith(FileUploadBase.MULTIPART))) {
        throw new InvalidContentTypeException(
            format(
                "the request doesn't contain a %s or %s stream, content type header is %s",
                FileUploadBase.MULTIPART_FORM_DATA, FileUploadBase.MULTIPART_MIXED, contentType));
      }

      @SuppressWarnings("deprecation") // still has to be backward compatible
      final int contentLengthInt = ctx.getContentLength();

      final long requestSize =
          UploadContext.class.isAssignableFrom(ctx.getClass())
              // Inline conditional is OK here CHECKSTYLE:OFF
              ? ((UploadContext) ctx).contentLength()
              : contentLengthInt;
      // CHECKSTYLE:ON

      InputStream input; // N.B. this is eventually closed in MultipartStream processing
      if (sizeMax >= 0) {
        if (requestSize != -1 && requestSize > sizeMax) {
          throw new SizeLimitExceededException(
              format(
                  "the request was rejected because its size (%s) exceeds the configured maximum (%s)",
                  Long.valueOf(requestSize), Long.valueOf(sizeMax)),
              requestSize,
              sizeMax);
        }
        // N.B. this is eventually closed in MultipartStream processing
        input =
            new LimitedInputStream(ctx.getInputStream(), sizeMax) {
              @Override
              protected void raiseError(long pSizeMax, long pCount) throws IOException {
                FileUploadException ex =
                    new SizeLimitExceededException(
                        format(
                            "the request was rejected because its size (%s) exceeds the configured maximum (%s)",
                            Long.valueOf(pCount), Long.valueOf(pSizeMax)),
                        pCount,
                        pSizeMax);
                throw new FileUploadIOException(ex);
              }
            };
      } else {
        input = ctx.getInputStream();
      }

      String charEncoding = headerEncoding;
      if (charEncoding == null) {
        charEncoding = ctx.getCharacterEncoding();
      }

      boundary = getBoundary(contentType);
      if (boundary == null) {
        IOUtils.closeQuietly(input); // avoid possible resource leak
        throw new FileUploadException(
            "the request was rejected because no multipart boundary was found");
      }

      try {
        multi = new MultipartStream(input, boundary, 4096, null);
      } catch (IllegalArgumentException iae) {
        IOUtils.closeQuietly(input); // avoid possible resource leak
        throw new InvalidContentTypeException(
            format(
                "The boundary specified in the %s header is too long", FileUploadBase.CONTENT_TYPE),
            iae);
      }
      multi.setHeaderEncoding(charEncoding);

      skipPreamble = true;
      findNextItem();
    }

    /**
     * Called for finding the next item, if any.
     *
     * @return True, if an next item was found, otherwise false.
     * @throws IOException An I/O error occurred.
     */
    private boolean findNextItem() throws IOException {
      if (eof) {
        return false;
      }
      if (currentItem != null) {
        currentItem.close();
        currentItem = null;
      }
      for (; ; ) {
        boolean nextPart;
        if (skipPreamble) {
          nextPart = multi.skipPreamble();
        } else {
          nextPart = multi.readBoundary();
        }
        if (!nextPart) {
          if (currentFieldName == null) {
            // Outer multipart terminated -> No more data
            eof = true;
            return false;
          }
          // Inner multipart terminated -> Return to parsing the outer
          multi.setBoundary(boundary);
          currentFieldName = null;
          continue;
        }
        FileItemHeaders headers = getParsedHeaders(multi.readHeaders());
        if (currentFieldName == null) {
          // We're parsing the outer multipart
          String fieldName = getFieldName(headers);
          if (fieldName != null) {
            String subContentType = headers.getHeader(FileUploadBase.CONTENT_TYPE);
            if (subContentType != null
                && subContentType
                    .toLowerCase(Locale.ENGLISH)
                    .startsWith(FileUploadBase.MULTIPART_MIXED)) {
              currentFieldName = fieldName;
              // Multiple files associated with this field name
              byte[] subBoundary = getBoundary(subContentType);
              multi.setBoundary(subBoundary);
              skipPreamble = true;
              continue;
            }
            String fileName = getFileName(headers);
            currentItem =
                new FileItemStreamImpl(
                    fileName,
                    fieldName,
                    headers.getHeader(FileUploadBase.CONTENT_TYPE),
                    fileName == null,
                    getContentLength(headers));
            currentItem.setHeaders(headers);
            itemValid = true;
            return true;
          }
        } else {
          String fileName = getFileName(headers);
          if (fileName != null) {
            currentItem =
                new FileItemStreamImpl(
                    fileName,
                    currentFieldName,
                    headers.getHeader(FileUploadBase.CONTENT_TYPE),
                    false,
                    getContentLength(headers));
            currentItem.setHeaders(headers);
            itemValid = true;
            return true;
          }
        }
        multi.discardBodyData();
      }
    }

    private long getContentLength(FileItemHeaders pHeaders) {
      try {
        return Long.parseLong(pHeaders.getHeader(FileUploadBase.CONTENT_LENGTH));
      } catch (Exception e) {
        return -1;
      }
    }

    /**
     * Returns, whether another instance of {@link FileItemStream} is available.
     *
     * @throws FileUploadException Parsing or processing the file item failed.
     * @throws IOException Reading the file item failed.
     * @return True, if one or more additional file items are available, otherwise false.
     */
    @Override
    public boolean hasNext() throws FileUploadException, IOException {
      if (eof) {
        return false;
      }
      if (itemValid) {
        return true;
      }
      try {
        return findNextItem();
      } catch (FileUploadIOException e) {
        // unwrap encapsulated SizeException
        throw (FileUploadException) e.getCause();
      }
    }

    /**
     * Returns the next available {@link FileItemStream}.
     *
     * @throws java.util.NoSuchElementException No more items are available. Use {@link #hasNext()}
     *     to prevent this exception.
     * @throws FileUploadException Parsing or processing the file item failed.
     * @throws IOException Reading the file item failed.
     * @return FileItemStream instance, which provides access to the next file item.
     */
    @Override
    public FileItemStream next() throws FileUploadException, IOException {
      if (eof || (!itemValid && !hasNext())) {
        throw new NoSuchElementException();
      }
      itemValid = false;
      return currentItem;
    }
  }
}
