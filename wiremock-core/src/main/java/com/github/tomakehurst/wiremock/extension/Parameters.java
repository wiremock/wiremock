/*
 * Copyright (C) 2015-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension;

import static com.github.tomakehurst.wiremock.common.ParameterUtils.checkParameter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.Metadata;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Parameters extends Metadata {

  public Parameters() {
    super();
  }

  @JsonCreator
  public Parameters(Map<? extends String, ?> data) {
    super(data);
  }

  @Override
  protected Metadata newInstance(Map<String, Object> value) {
    return new Parameters(value);
  }

  public static Parameters empty() {
    return new Parameters();
  }

  public static Parameters from(Map<String, Object> parameterMap) {
    return new Parameters(parameterMap);
  }

  public static Parameters one(String name, Object value) {
    return from(Map.of(name, value));
  }

  public static <T> Parameters of(T myData) {
    return from(Json.objectToMap(myData));
  }

  @SuppressWarnings("unchecked")
  public Parameters getParameters(String key) {
    checkKeyPresent(key);
    checkParameter(Map.class.isAssignableFrom(get(key).getClass()), key + " is not a map");
    return new Parameters((Map<String, ?>) get(key));
  }

  @SuppressWarnings("unchecked")
  public Parameters getParameters(String key, Parameters defaultValue) {
    if (!containsKey(key)) {
      return defaultValue;
    }

    checkParameter(Map.class.isAssignableFrom(get(key).getClass()), key + " is not a map");
    return new Parameters((Map<String, ?>) get(key));
  }

  public Parameters transform(Consumer<Builder> transformer) {
    final Builder builder = new Builder(this);
    transformer.accept(builder);
    return Parameters.from(builder.build());
  }

  public Parameters merge(Parameters other) {
    Map<String, Object> attributes = new LinkedHashMap<>(this);
    attributes.putAll(other);
    return Parameters.from(attributes);
  }

  public Parameters deepMerge(Parameters other) {
    return from(super.deepMerge(other));
  }
}
