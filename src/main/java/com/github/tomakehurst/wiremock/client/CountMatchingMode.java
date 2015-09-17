package com.github.tomakehurst.wiremock.client;

import com.github.tomakehurst.wiremock.common.BiPredicate;

/**
 *
 */
public interface CountMatchingMode extends BiPredicate<Integer, Integer> {

    String getFriendlyName();

}
