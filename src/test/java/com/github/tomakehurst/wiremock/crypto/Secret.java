package com.github.tomakehurst.wiremock.crypto;

import java.util.Arrays;

import static java.util.Arrays.copyOf;
import static java.util.Arrays.fill;
import static java.util.Objects.requireNonNull;

public class Secret implements AutoCloseable {

    private static final char[] EMPTY_VALUE = new char[0];
    private volatile char[] value;

    public Secret(char[] value) {
        requireNonNull(value, "Secret value may not be null");

        this.value = copyOf(value, value.length);
    }

    public Secret(String value) {
        this(null == value ? null : value.toCharArray());
    }

    public char[] getValue() {
        return Arrays.copyOf(value, value.length);
    }

    @Override
    public void close() {
        if (EMPTY_VALUE == value)
            return;

        char[] tempValue = value;
        value = EMPTY_VALUE;

        fill(tempValue, (char) 0x00);
    }

    public boolean compareTo(String password) {
        if (password == null) {
            return false;
        }

        return Arrays.equals(password.toCharArray(), value);
    }
}
