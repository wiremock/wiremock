package com.github.tomakehurst.wiremock.extension;


public interface MetricsPublisher extends Extension {
  boolean summary(String metricName, double metricData, String... tags);
}
