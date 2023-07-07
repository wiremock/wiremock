/*
 * Copyright (C) 2016-2023 Thomas Akehurst
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

import java.net.URLEncoder;

public class Encoding {

  private Encoding() {}

  private static Base64Encoder encoder = null;

  private static Base64Encoder getInstance() {
    if (encoder == null) {
      synchronized (Encoding.class) {
        if (encoder == null) {
          encoder = new JdkBase64Encoder();
        }
      }
    }

    return encoder;
  }

  public static byte[] decodeBase64(String base64) {
    return base64 != null ? getInstance().decode(base64) : null;
  }

  public static String encodeBase64(byte[] content) {
    return encodeBase64(content, true);
  }

  public static String encodeBase64(byte[] content, boolean padding) {
    return content != null ? getInstance().encode(content, padding) : null;
  }

  public static String urlEncode(String encodedUrl) {
    return URLEncoder.encode(encodedUrl, UTF_8);
  }
}
