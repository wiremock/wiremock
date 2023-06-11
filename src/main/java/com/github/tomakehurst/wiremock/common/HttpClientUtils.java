/*
 * Copyright (C) 2011-2023 Thomas Akehurst
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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

public class HttpClientUtils {

  private HttpClientUtils() {}

  public static String getEntityAsStringAndCloseStream(ClassicHttpResponse httpResponse) {
    HttpEntity entity = httpResponse.getEntity();
    if (entity != null) {
      try {
        String content = EntityUtils.toString(entity, UTF_8);
        entity.getContent().close();
        return content;
      } catch (IOException | ParseException ioe) {
        throw new RuntimeException(ioe);
      }
    }

    return null;
  }

  public static byte[] getEntityAsByteArrayAndCloseStream(ClassicHttpResponse httpResponse) {
    HttpEntity entity = httpResponse.getEntity();
    if (entity != null) {
      try {
        byte[] content = EntityUtils.toByteArray(entity);
        entity.getContent().close();
        return content;
      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }
    }

    return null;
  }
}
