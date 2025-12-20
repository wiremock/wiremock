package org.wiremock.url;

class HostAndPortParser implements CharSequenceParser<HostAndPort> {

  static final HostAndPortParser INSTANCE = new HostAndPortParser();

  @Override
  public HostAndPort parse(CharSequence stringForm) throws ParseException {
    String hostAndPortStr = stringForm.toString();
    var authority = Authority.parse(hostAndPortStr);
    if (authority instanceof HostAndPort) {
      return (HostAndPort) authority;
    } else {
      throw new IllegalHostAndPort(hostAndPortStr);
    }
  }
}
