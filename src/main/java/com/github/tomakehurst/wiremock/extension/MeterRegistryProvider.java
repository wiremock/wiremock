package com.github.tomakehurst.wiremock.extension;

import io.micrometer.core.instrument.MeterRegistry;

public interface MeterRegistryProvider extends Extension {
  MeterRegistry getMeterRegistry();
}
