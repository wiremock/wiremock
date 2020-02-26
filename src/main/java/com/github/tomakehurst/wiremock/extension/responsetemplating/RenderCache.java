package com.github.tomakehurst.wiremock.extension.responsetemplating;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Arrays.asList;

public class RenderCache {

    private final Map<Key, Object> cache = new HashMap<>();

    public void put(Key key, Object value) {
        cache.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Key key) {
        return (T) cache.get(key);
    }

    public static class Key {
        private final Class<?> forClass;
        private final List<?> elements;

        public static Key keyFor(Class<?> forClass, Object... elements) {
            return new Key(forClass, asList(elements));
        }

        private Key(Class<?> forClass, List<?> elements) {
            this.forClass = forClass;
            this.elements = elements;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Key{");
            sb.append("forClass=").append(forClass);
            sb.append(", elements=").append(elements);
            sb.append('}');
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return forClass.equals(key.forClass) &&
                    elements.equals(key.elements);
        }

        @Override
        public int hashCode() {
            return Objects.hash(forClass, elements);
        }
    }
}
