package org.wiremock.url;

public class IllegalUserInfo extends IllegalUrlPart {

  public IllegalUserInfo(String invalid) {
    super(invalid, "Illegal user info: `" + invalid + "`");
  }
}
