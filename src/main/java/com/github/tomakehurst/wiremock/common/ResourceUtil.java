/*
 * Copyright (C) 2023 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.common.ParameterUtils.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceUtil {

  private ResourceUtil() {}

  public static <T> ClassLoader getLoader(Class<T> className) {
    return getFirstNonNull(
        className.getClassLoader(), Thread.currentThread().getContextClassLoader());
  }

  public static <T> URL getResource(Class<T> className, String resourceName) {
    ClassLoader loader = getLoader(className);
    URL url = loader.getResource(resourceName);
    checkParameter(url != null, String.format("resource %s not found.", resourceName));
    return loader.getResource(resourceName);
  }

  public static <T> URI getResourceURI(Class<T> className, String resourceName) {
    try {
      return getResource(className, resourceName).toURI();
    } catch (URISyntaxException e) {
      return throwUnchecked(e, URI.class);
    }
  }

  public static <T> Path getResourcePath(Class<T> className, String resourceName) {
    return Paths.get(getResourceURI(className, resourceName));
  }
}
