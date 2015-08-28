package com.github.tomakehurst.wiremock.common;

/**
 *
 */
public interface BiPredicate<T, U> {

    boolean test(T first, U second);

}
