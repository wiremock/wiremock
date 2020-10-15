package com.github.tomakehurst.wiremock.extension;


import com.sun.istack.internal.Nullable;

import java.util.function.ToDoubleFunction;

public interface MetricsPublisher extends Extension {
  boolean summary(String metricName, double metricData, String... tags);

  boolean counter(String name, String... tags);

  <T> T gauge(String name, @Nullable T stateObject, ToDoubleFunction<T> valueFunction, String... tags);
}
