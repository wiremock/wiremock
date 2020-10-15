package com.github.tomakehurst.wiremock.extension;


import java.util.function.ToDoubleFunction;

public interface MetricsPublisher extends Extension {
  boolean summary(String metricName, double metricData, String... tags);

  boolean counter(String name, String... tags);

  <T> T gauge(String name, T stateObject, ToDoubleFunction<T> valueFunction, String... tags);
}
