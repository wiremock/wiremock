package com.github.tomakehurst.wiremock.common;

public class Exceptions {

    public static RuntimeException throwUnchecked(final Exception ex) {
        Exceptions.<RuntimeException>throwsUnchecked(ex);
        throw new AssertionError("This code should be unreachable. Something went terrible wrong here!");
    }

    @SuppressWarnings("unchecked")
    public static <T extends Exception> void throwsUnchecked(Exception toThrow) throws T {
        throw (T) toThrow;
    }
}
