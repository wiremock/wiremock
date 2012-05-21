package com.github.tomakehurst.wiremock.common;

import static java.lang.System.nanoTime;

public class Timer {

	public static String millisecondsFrom(long start) {
		long durationNanos = nanoTime() - start;
		return String.valueOf(durationNanos / 1000000.0);
	}
}
