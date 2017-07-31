package com.github.tomakehurst.wiremock.http;

public class HttpStatus {

    public static boolean isSuccess(int code) {
        return ((200 <= code) && (code <= 299));
    }

    public static boolean isRedirection(int code) {
        return ((300 <= code) && (code <= 399));
    }

    public static boolean isClientError(int code) {
        return ((400 <= code) && (code <= 499));
    }

    public static boolean isServerError(int code) {
        return ((500 <= code) && (code <= 599));
    }
}
