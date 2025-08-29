/*
 * Copyright (C) 2018-2025 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.ParameterUtils.checkParameter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The type Metadata. */
public class Metadata extends LinkedHashMap<String, Object> {

  /** Instantiates a new Metadata. */
  public Metadata() {}

  /**
   * Instantiates a new Metadata.
   *
   * @param data the data
   */
  public Metadata(Map<? extends String, ?> data) {
    super(data);
  }

  /**
   * Gets int.
   *
   * @param key the key
   * @return the int
   */
  public Integer getInt(String key) {
    return checkPresenceValidityAndCast(key, Integer.class);
  }

  /**
   * Gets int.
   *
   * @param key the key
   * @param defaultValue the default value
   * @return the int
   */
  public Integer getInt(String key, Integer defaultValue) {
    return returnIfValidOrDefaultIfNot(key, Integer.class, defaultValue);
  }

  /**
   * Gets boolean.
   *
   * @param key the key
   * @return the boolean
   */
  public Boolean getBoolean(String key) {
    return checkPresenceValidityAndCast(key, Boolean.class);
  }

  /**
   * Gets boolean.
   *
   * @param key the key
   * @param defaultValue the default value
   * @return the boolean
   */
  public Boolean getBoolean(String key, Boolean defaultValue) {
    return returnIfValidOrDefaultIfNot(key, Boolean.class, defaultValue);
  }

  /**
   * Gets string.
   *
   * @param key the key
   * @return the string
   */
  public String getString(String key) {
    return checkPresenceValidityAndCast(key, String.class);
  }

  /**
   * Gets string.
   *
   * @param key the key
   * @param defaultValue the default value
   * @return the string
   */
  public String getString(String key, String defaultValue) {
    return returnIfValidOrDefaultIfNot(key, String.class, defaultValue);
  }

  /**
   * Gets list.
   *
   * @param key the key
   * @return the list
   */
  public List<?> getList(String key) {
    return checkPresenceValidityAndCast(key, List.class);
  }

  /**
   * Gets metadata.
   *
   * @param key the key
   * @return the metadata
   */
  @SuppressWarnings("unchecked")
  public Metadata getMetadata(String key) {
    checkKeyPresent(key);
    checkParameter(Map.class.isAssignableFrom(get(key).getClass()), key + " is not a map");
    return new Metadata((Map<String, ?>) get(key));
  }

  /**
   * Gets metadata.
   *
   * @param key the key
   * @param defaultValue the default value
   * @return the metadata
   */
  public Metadata getMetadata(String key, Metadata defaultValue) {
    if (!containsKey(key)) {
      return defaultValue;
    }

    checkParameter(Map.class.isAssignableFrom(get(key).getClass()), key + " is not a map");
    return new Metadata((Map<String, ?>) get(key));
  }

  @SuppressWarnings("unchecked")
  private <T> T checkPresenceValidityAndCast(String key, Class<T> type) {
    checkKeyPresent(key);
    checkParameter(
        type.isAssignableFrom(get(key).getClass()),
        key + " is not of type " + type.getSimpleName());
    return (T) get(key);
  }

  @SuppressWarnings("unchecked")
  private <T> T returnIfValidOrDefaultIfNot(String key, Class<T> type, T defaultValue) {
    if (!containsKey(key) || !type.isAssignableFrom(get(key).getClass())) {
      return defaultValue;
    }

    return (T) get(key);
  }

  private void checkKeyPresent(String key) {
    checkParameter(containsKey(key), key + "' not present");
  }

  /**
   * From metadata.
   *
   * @param <T> the type parameter
   * @param myData the my data
   * @return the metadata
   */
  public static <T> Metadata from(T myData) {
    return new Metadata(Json.objectToMap(myData));
  }

  /**
   * Metadata builder.
   *
   * @return the builder
   */
  public static Builder metadata() {
    return new Builder();
  }

  /**
   * As t.
   *
   * @param <T> the type parameter
   * @param myDataClass the my data class
   * @return the t
   */
  public <T> T as(Class<T> myDataClass) {
    return Json.mapToObject(this, myDataClass);
  }

  /** The type Builder. */
  public static class Builder {

    private final Map<String, Object> mapBuilder;

    /** Instantiates a new Builder. */
    public Builder() {
      this.mapBuilder = new LinkedHashMap<>();
    }

    /**
     * Attr builder.
     *
     * @param key the key
     * @param value the value
     * @return the builder
     */
    public Builder attr(String key, Object value) {
      mapBuilder.put(key, value);
      return this;
    }

    /**
     * List builder.
     *
     * @param key the key
     * @param values the values
     * @return the builder
     */
    public Builder list(String key, Object... values) {
      mapBuilder.put(key, List.of(values));
      return this;
    }

    /**
     * Attr builder.
     *
     * @param key the key
     * @param metadataBuilder the metadata builder
     * @return the builder
     */
    public Builder attr(String key, Metadata.Builder metadataBuilder) {
      mapBuilder.put(key, metadataBuilder.build());
      return this;
    }

    /**
     * Build metadata.
     *
     * @return the metadata
     */
    public Metadata build() {
      return new Metadata(mapBuilder);
    }
  }
}
