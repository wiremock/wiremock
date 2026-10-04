/*
 * Copyright (C) 2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ignored.DefaultHttpsKeystoreTestFixture;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DefaultHttpsKeystoreAcceptanceTest {

  @TempDir Path classpathRoot;

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void defaultKeystoreIsNotShadowedByApplicationResources(boolean directory) throws Exception {
    Path conflictingResource = classpathRoot.resolve("keystore");
    if (directory) {
      Files.createDirectory(conflictingResource);
      Files.writeString(conflictingResource.resolve("application.txt"), "Application resource");
    } else {
      Files.writeString(conflictingResource, "Not a Java keystore");
    }

    try (URLClassLoader loader =
        new URLClassLoader(classpathUrls(), ClassLoader.getPlatformClassLoader())) {
      assertEquals(conflictingResource, Path.of(loader.getResource("keystore").toURI()));
      Thread thread = Thread.currentThread();
      ClassLoader originalLoader = thread.getContextClassLoader();
      try {
        thread.setContextClassLoader(loader);
        loader
            .loadClass(DefaultHttpsKeystoreTestFixture.class.getName())
            .getMethod("verifyHttpsDefaults")
            .invoke(null);
      } catch (InvocationTargetException e) {
        throw new AssertionError(
            "HTTPS defaults failed with a conflicting classpath resource", e.getCause());
      } finally {
        thread.setContextClassLoader(originalLoader);
      }
    }
  }

  private URL[] classpathUrls() throws Exception {
    Set<URL> urls = new LinkedHashSet<>();
    urls.add(classpathRoot.toUri().toURL());
    for (ClassLoader loader = getClass().getClassLoader();
        loader != null;
        loader = loader.getParent()) {
      if (loader instanceof URLClassLoader urlClassLoader) {
        urls.addAll(asList(urlClassLoader.getURLs()));
      }
    }
    for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
      urls.add(Path.of(entry).toUri().toURL());
    }
    return urls.toArray(URL[]::new);
  }
}
