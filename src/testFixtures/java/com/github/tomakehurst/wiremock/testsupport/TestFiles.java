/*
 * Copyright (C) 2017-2024 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.common.ResourceUtil.getResourcePath;
import static com.github.tomakehurst.wiremock.common.ResourceUtil.getResourceURI;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TestFiles {

  public static final String TRUST_STORE_PASSWORD = "mytruststorepassword";
  public static final String TRUST_STORE_NAME = getTrustStoreRelativeName();
  public static final String JCEKS_TRUST_STORE_NAME = "test-truststore.jceks";
  public static final String TRUST_STORE_PATH = filePath(TRUST_STORE_NAME);
  public static final String KEY_STORE_PATH = filePath("test-keystore");
  public static final String KEY_STORE_WITH_CA_PATH = filePath("test-keystore-with-ca");

  private static String getTrustStoreRelativeName() {
    return System.getProperty("java.specification.version").equals("1.7")
        ? "test-truststore.jks"
        : "test-truststore.pkcs12";
  }

  public static String defaultTestFilesRoot() {
    return filePath("test-file-root");
  }

  public static String file(String path) {
    try {
      String text = Files.readString(getResourcePath(TestFiles.class, path), UTF_8);
      if (System.getProperty("os.name").startsWith("Windows")) {
        text = text.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
      }

      return text;
    } catch (IOException e) {
      return throwUnchecked(e, String.class);
    }
  }

  public static String filePath(String path) {
    return new File(getResourceURI(TestFiles.class, path)).getAbsolutePath();
  }

  public static String sampleWarRootDir() {
    return new File("sample-war").exists() ? "sample-war" : "../sample-war";
  }
}
