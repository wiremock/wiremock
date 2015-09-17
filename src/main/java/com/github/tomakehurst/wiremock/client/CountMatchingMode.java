package com.github.tomakehurst.wiremock.client;

import java.util.function.BiPredicate;

/**
 *
 */
public interface CountMatchingMode extends BiPredicate<Integer, Integer> {

    String getFriendlyName();

}
