package com.github.tomakehurst.wiremock.client;

public class TestResourceDTO {

  private final String attrString;

  public TestResourceDTO(final String attrString) {
    this.attrString = attrString;
  }

  public String getAttrString() {
    return attrString;
  }
}
