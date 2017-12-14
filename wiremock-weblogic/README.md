WireMock - Weblogic 12.1.3.0.0 installation
======================================================

[![Build Status](https://travis-ci.org/tomakehurst/wiremock.svg?branch=master)](https://travis-ci.org/tomakehurst/wiremock)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.tomakehurst/wiremock/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.tomakehurst/wiremock)



Building WireMock for weblogic locally
--------------------------------------
To build both war and explodedWar
```bash
./gradlew clean war explodedWar
```

The built files will be placed under ``build/libs`` (for the war) and ``build/wiremock`` (for the exploded war)
