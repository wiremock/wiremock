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
import static java.util.Collections.emptyMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class Metadata implements Map<String, Object> {

  private final Map<String, Object> data;

  public Metadata() {
    this.data = emptyMap();
  }

  @JsonCreator
  public Metadata(Map<? extends String, ?> data) {
    this.data = Collections.unmodifiableMap(convertNestedMapsToMetadata(data));
  }

  @SuppressWarnings("unchecked")
  protected Map<String, Object> convertNestedMapsToMetadata(Map<? extends String, ?> data) {
    if (data == null) {
      return emptyMap();
    }

    Map<String, Object> result = new LinkedHashMap<>();
    for (Map.Entry<? extends String, ?> entry : data.entrySet()) {
      Object value = entry.getValue();
      if (value instanceof Map && !(this.getClass().isInstance(value))) {
        result.put(entry.getKey(), newInstance((Map<String, Object>) value));
      } else {
        result.put(entry.getKey(), value);
      }
    }
    return result;
  }

  protected Metadata newInstance(Map<String, Object> value) {
    return new Metadata(value);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Metadata create(Consumer<Builder> transformer) {
    final Builder builder = builder();
    transformer.accept(builder);
    return builder.build();
  }

  public Metadata transform(Consumer<Builder> transformer) {
    final Builder builder = toBuilder();
    transformer.accept(builder);
    return builder.build();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public Integer getInt(String key) {
    return checkPresenceValidityAndCast(key, Integer.class);
  }

  public Integer getInt(String key, Integer defaultValue) {
    return returnIfValidOrDefaultIfNot(key, Integer.class, defaultValue);
  }

  public Boolean getBoolean(String key) {
    return checkPresenceValidityAndCast(key, Boolean.class);
  }

  public Boolean getBoolean(String key, Boolean defaultValue) {
    return returnIfValidOrDefaultIfNot(key, Boolean.class, defaultValue);
  }

  public String getString(String key) {
    return checkPresenceValidityAndCast(key, String.class);
  }

  public String getString(String key, String defaultValue) {
    return returnIfValidOrDefaultIfNot(key, String.class, defaultValue);
  }

  public List<?> getList(String key) {
    return checkPresenceValidityAndCast(key, List.class);
  }

  @SuppressWarnings("unchecked")
  public Metadata getMetadata(String key) {
    checkKeyPresent(key);
    checkParameter(Map.class.isAssignableFrom(get(key).getClass()), key + " is not a map");
    return new Metadata((Map<String, ?>) get(key));
  }

  @SuppressWarnings("unchecked")
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

  protected void checkKeyPresent(String key) {
    checkParameter(containsKey(key), key + "' not present");
  }

  public static <T> Metadata from(T myData) {
    return new Metadata(Json.objectToMap(myData));
  }

  public static Builder metadata() {
    return new Builder();
  }

  public <T> T as(Class<T> myDataClass) {
    return Json.mapToObject(this, myDataClass);
  }

  // Map interface implementation - delegate to immutable data map
  @Override
  public int size() {
    return data.size();
  }

  @Override
  public boolean isEmpty() {
    return data.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return data.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return data.containsValue(value);
  }

  @Override
  public Object get(Object key) {
    return data.get(key);
  }

  @Override
  public Set<String> keySet() {
    return data.keySet();
  }

  @Override
  public Collection<Object> values() {
    return data.values();
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
    return data.entrySet();
  }

  @Override
  public Object getOrDefault(Object key, Object defaultValue) {
    return data.getOrDefault(key, defaultValue);
  }

  @Override
  public void forEach(BiConsumer<? super String, ? super Object> action) {
    data.forEach(action);
  }

  // Unsupported mutating operations
  @Override
  public Object put(String key, Object value) {
    throw new UnsupportedOperationException("Metadata is immutable");
  }

  @Override
  public Object remove(Object key) {
    throw new UnsupportedOperationException("Metadata is immutable");
  }

  @Override
  public void putAll(Map<? extends String, ?> m) {
    throw new UnsupportedOperationException("Metadata is immutable");
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException("Metadata is immutable");
  }

  @Override
  public void replaceAll(BiFunction<? super String, ? super Object, ?> function) {
    throw new UnsupportedOperationException("Metadata is immutable");
  }

  @Override
  public Object putIfAbsent(String key, Object value) {
    throw new UnsupportedOperationException("Metadata is immutable");
  }

  @Override
  public boolean remove(Object key, Object value) {
    throw new UnsupportedOperationException("Metadata is immutable");
  }

  @Override
  public boolean replace(String key, Object oldValue, Object newValue) {
    throw new UnsupportedOperationException("Metadata is immutable");
  }

  @Override
  public Object replace(String key, Object value) {
    throw new UnsupportedOperationException("Metadata is immutable");
  }

  @Override
  public Object computeIfAbsent(String key, Function<? super String, ?> mappingFunction) {
    throw new UnsupportedOperationException("Metadata is immutable");
  }

  @Override
  public Object computeIfPresent(
      String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
    throw new UnsupportedOperationException("Metadata is immutable");
  }

  @Override
  public Object compute(
      String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
    throw new UnsupportedOperationException("Metadata is immutable");
  }

  @Override
  public Object merge(
      String key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
    throw new UnsupportedOperationException("Metadata is immutable");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Metadata metadata = (Metadata) o;
    return data.equals(metadata.data);
  }

  @Override
  public int hashCode() {
    return data.hashCode();
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Metadata.class.getSimpleName() + "[", "]")
        .add("data=" + data)
        .toString();
  }

  public Metadata deepMerge(Metadata toMerge) {
    return transform(
        builder -> {
          for (Map.Entry<String, Object> entry : toMerge.entrySet()) {
            if (entry.getValue() instanceof Metadata) {
              Object existing = get(entry.getKey());
              if (existing instanceof Metadata) {
                builder.attr(
                    entry.getKey(), ((Metadata) existing).deepMerge((Metadata) entry.getValue()));
              } else {
                builder.attr(entry.getKey(), entry.getValue());
              }
            } else if (entry.getValue() instanceof List<?>) {
              Object existing = get(entry.getKey());
              if (existing instanceof List<?>) {
                List<Object> merged = new ArrayList<>((List<?>) existing);
                merged.addAll((List<?>) entry.getValue());
                builder.attr(entry.getKey(), merged);
              } else {
                builder.attr(entry.getKey(), entry.getValue());
              }
            } else {
              builder.attr(entry.getKey(), entry.getValue());
            }
          }
        });
  }

  public Map<String, Object> asMutableMap() {
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    forEach(
        (key, value) -> {
          if (this.getClass().isInstance(value)) {
            map.put(key, (this.getClass().cast(value)).asMutableMap());
          } else {
            map.put(key, value);
          }
        });

    return map;
  }

  public static class Builder {

    private final Map<String, Object> mapBuilder;

    public Builder() {
      this.mapBuilder = new LinkedHashMap<>();
    }

    public Builder(Metadata existing) {
      this.mapBuilder = new LinkedHashMap<>(existing.data);
    }

    public Builder attr(String key, Object value) {
      mapBuilder.put(key, value);
      return this;
    }

    public Builder attr(String key, Consumer<Builder> transformer) {
      final Object existing = get(key);
      final Builder builder =
          existing instanceof Metadata ? new Builder((Metadata) existing) : builder();
      transformer.accept(builder);
      attr(key, builder);
      return this;
    }

    public Builder attr(String key, Builder metadataBuilder) {
      mapBuilder.put(key, metadataBuilder.build());
      return this;
    }

    public Builder list(String key, Object... values) {
      mapBuilder.put(key, List.of(values));
      return this;
    }

    public Object get(String key) {
      return mapBuilder.get(key);
    }

    public boolean contains(String key) {
      return mapBuilder.containsKey(key);
    }

    public void remove(String key) {
      mapBuilder.remove(key);
    }

    public Metadata build() {
      return new Metadata(mapBuilder);
    }
  }
}
