package com.github.tomakehurst.wiremock.core;

import java.io.IOException;
import java.util.Properties;

public class Version {
  public static String getCurrentVersion() {
    Properties properties = new Properties();
    try {
      properties.load(Version.class.getResourceAsStream("/version.properties"));
      return properties.getProperty("version");
    } catch (IOException e) {
        return "unknown";
    }
  }
}
