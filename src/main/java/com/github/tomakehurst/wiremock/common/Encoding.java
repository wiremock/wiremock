/*
 * Copyright (C) 2016-2022 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;

public class Encoding {

  public static byte[] decodeBase64(String base64) {
    return base64 != null ? Base64.getDecoder().decode(base64) : null;
  }

  public static String encodeBase64(byte[] content) {
    return encodeBase64(content, true);
  }

  public static String encodeBase64(byte[] content, boolean padding) {
    Base64.Encoder encoder = getEncoder(padding);

    return content != null ? encoder.encodeToString(content) : null;
  }

  public static String urlEncode(String unencodedUrl) {
    try {
      return URLEncoder.encode(unencodedUrl, "utf-8");
    } catch (UnsupportedEncodingException e) {
      return throwUnchecked(e, String.class);
    }
  }

  private static Base64.Encoder getEncoder(boolean padding) {
    if (!padding) {
      return Base64.getEncoder().withoutPadding();
    } else {
      return Base64.getEncoder();
    }
  }
}
