/*
 * Copyright (C) 2025-2025 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wiremock.url;

import java.util.regex.Pattern;

/**
 * A strict RFC 3986 URI-reference validator.
 *
 * <p>This validates the syntax defined <a
 * href="https://www.rfc-editor.org/rfc/rfc3986#appendix-A">RFC 3986 Appendix A.</a>
 *
 * <p>Key grammar rules implemented:
 *
 * <p>URI-reference = URI / relative-ref URI = scheme ":" hier-part [ "?" query ] [ "#" fragment ]
 * relative-ref = relative-part [ "?" query ] [ "#" fragment ]
 *
 * <p>hier-part = "//" authority path-abempty / path-absolute / path-rootless / path-empty
 *
 * <p>authority = [ userinfo "@" ] host [ ":" port ] host = IP-literal / IPv4address / reg-name
 * IP-literal = "[" ( IPv6address / IPvFuture ) "]"
 */
public final class Rfc3986Validator {

  private Rfc3986Validator() {}

  // ===========================================
  // Character classes (RFC 3986 Section 2)
  // ===========================================

  // ALPHA = A-Za-z
  private static final String ALPHA = "A-Za-z";

  // DIGIT = 0-9
  private static final String DIGIT = "0-9";

  // HEXDIG = DIGIT / "A" / "B" / "C" / "D" / "E" / "F" (case-insensitive)
  private static final String HEXDIG = "0-9A-Fa-f";

  // unreserved = ALPHA / DIGIT / "-" / "." / "_" / "~"
  private static final String UNRESERVED = ALPHA + DIGIT + "\\-._~";

  // sub-delims = "!" / "$" / "&" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="
  private static final String SUB_DELIMS = "!$&'()*+,;=";

  // pct-encoded = "%" HEXDIG HEXDIG
  private static final String PCT_ENCODED = "%[" + HEXDIG + "]{2}";

  // ===========================================
  // URI Components
  // ===========================================

  // scheme = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )
  private static final String SCHEME = "[" + ALPHA + "][" + ALPHA + DIGIT + "+\\-.]*";

  // userinfo = *( unreserved / pct-encoded / sub-delims / ":" )
  private static final String USERINFO_CHAR = "[" + UNRESERVED + SUB_DELIMS + ":]";
  private static final String USERINFO = "(?:" + USERINFO_CHAR + "|" + PCT_ENCODED + ")*";

  // ===========================================
  // IP Address Validation (the hard part)
  // ===========================================

  // dec-octet: 0-255
  // dec-octet = DIGIT                 ; 0-9
  //           / %x31-39 DIGIT         ; 10-99
  //           / "1" 2DIGIT            ; 100-199
  //           / "2" %x30-34 DIGIT     ; 200-249
  //           / "25" %x30-35          ; 250-255
  private static final String DEC_OCTET = "(?:25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]|[0-9])";

  // IPv4address = dec-octet "." dec-octet "." dec-octet "." dec-octet
  private static final String IPV4_ADDRESS =
      DEC_OCTET + "\\." + DEC_OCTET + "\\." + DEC_OCTET + "\\." + DEC_OCTET;

  // h16 = 1*4HEXDIG
  private static final String H16 = "[" + HEXDIG + "]{1,4}";

  // ls32 = ( h16 ":" h16 ) / IPv4address
  private static final String LS32 = "(?:" + H16 + ":" + H16 + "|" + IPV4_ADDRESS + ")";

  // IPv6address =                            6( h16 ":" ) ls32
  //             /                       "::" 5( h16 ":" ) ls32
  //             / [               h16 ] "::" 4( h16 ":" ) ls32
  //             / [ *1( h16 ":" ) h16 ] "::" 3( h16 ":" ) ls32
  //             / [ *2( h16 ":" ) h16 ] "::" 2( h16 ":" ) ls32
  //             / [ *3( h16 ":" ) h16 ] "::"    h16 ":"   ls32
  //             / [ *4( h16 ":" ) h16 ] "::"              ls32
  //             / [ *5( h16 ":" ) h16 ] "::"              h16
  //             / [ *6( h16 ":" ) h16 ] "::"
  private static final String IPV6_ADDRESS = buildIpv6Pattern();

  private static String buildIpv6Pattern() {
    String h16c = H16 + ":"; // h16 followed by colon

    // Build each alternative
    String[] alts = {
      // 6( h16 ":" ) ls32
      "(?:" + h16c + "){6}" + LS32,

      // "::" 5( h16 ":" ) ls32
      "::(?:" + h16c + "){5}" + LS32,

      // [ h16 ] "::" 4( h16 ":" ) ls32
      "(?:" + H16 + ")?::(?:" + h16c + "){4}" + LS32,

      // [ *1( h16 ":" ) h16 ] "::" 3( h16 ":" ) ls32
      "(?:(?:" + h16c + "){0,1}" + H16 + ")?::(?:" + h16c + "){3}" + LS32,

      // [ *2( h16 ":" ) h16 ] "::" 2( h16 ":" ) ls32
      "(?:(?:" + h16c + "){0,2}" + H16 + ")?::(?:" + h16c + "){2}" + LS32,

      // [ *3( h16 ":" ) h16 ] "::" h16 ":" ls32
      "(?:(?:" + h16c + "){0,3}" + H16 + ")?::" + h16c + LS32,

      // [ *4( h16 ":" ) h16 ] "::" ls32
      "(?:(?:" + h16c + "){0,4}" + H16 + ")?::" + LS32,

      // [ *5( h16 ":" ) h16 ] "::" h16
      "(?:(?:" + h16c + "){0,5}" + H16 + ")?::" + H16,

      // [ *6( h16 ":" ) h16 ] "::"
      "(?:(?:" + h16c + "){0,6}" + H16 + ")?::"
    };

    return "(?:" + String.join("|", alts) + ")";
  }

  // IPvFuture = "v" 1*HEXDIG "." 1*( unreserved / sub-delims / ":" )
  private static final String IPV_FUTURE =
      "v[" + HEXDIG + "]+\\.[" + UNRESERVED + SUB_DELIMS + ":]+";

  // IP-literal = "[" ( IPv6address / IPvFuture ) "]"
  private static final String IP_LITERAL = "\\[(?:" + IPV6_ADDRESS + "|" + IPV_FUTURE + ")\\]";

  // reg-name = *( unreserved / pct-encoded / sub-delims )
  private static final String REG_NAME_CHAR = "[" + UNRESERVED + SUB_DELIMS + "]";
  private static final String REG_NAME = "(?:" + REG_NAME_CHAR + "|" + PCT_ENCODED + ")*";

  // host = IP-literal / IPv4address / reg-name
  // Note: Order matters - we try IP-literal first, then IPv4, then reg-name
  private static final String HOST = "(?:" + IP_LITERAL + "|" + IPV4_ADDRESS + "|" + REG_NAME + ")";

  // port = *DIGIT
  private static final String PORT = "[" + DIGIT + "]*";

  // authority = [ userinfo "@" ] host [ ":" port ]
  private static final String AUTHORITY = "(?:" + USERINFO + "@)?" + HOST + "(?::" + PORT + ")?";

  // ===========================================
  // Path Components
  // ===========================================

  // pchar = unreserved / pct-encoded / sub-delims / ":" / "@"
  private static final String PCHAR = "(?:[" + UNRESERVED + SUB_DELIMS + ":@]|" + PCT_ENCODED + ")";

  // segment = *pchar
  private static final String SEGMENT = PCHAR + "*";

  // segment-nz = 1*pchar
  private static final String SEGMENT_NZ = PCHAR + "+";

  // segment-nz-nc = 1*( unreserved / pct-encoded / sub-delims / "@" )
  //               ; non-zero-length segment without any colon ":"
  private static final String SEGMENT_NZ_NC =
      "(?:[" + UNRESERVED + SUB_DELIMS + "@]|" + PCT_ENCODED + ")+";

  // path-abempty  = *( "/" segment )
  private static final String PATH_ABEMPTY = "(?:/" + SEGMENT + ")*";

  // path-absolute = "/" [ segment-nz *( "/" segment ) ]
  private static final String PATH_ABSOLUTE = "/(?:" + SEGMENT_NZ + "(?:/" + SEGMENT + ")*)?";

  // path-noscheme = segment-nz-nc *( "/" segment )
  private static final String PATH_NOSCHEME = SEGMENT_NZ_NC + "(?:/" + SEGMENT + ")*";

  // path-rootless = segment-nz *( "/" segment )
  private static final String PATH_ROOTLESS = SEGMENT_NZ + "(?:/" + SEGMENT + ")*";

  // path-empty = 0<pchar>
  private static final String PATH_EMPTY = "";

  // ===========================================
  // Query and Fragment
  // ===========================================

  // query = *( pchar / "/" / "?" )
  private static final String QUERY = "(?:" + PCHAR + "|[/?])*";

  // fragment = *( pchar / "/" / "?" )
  private static final String FRAGMENT = QUERY; // Same as query

  // ===========================================
  // Hierarchical Part
  // ===========================================

  // hier-part = "//" authority path-abempty
  //           / path-absolute
  //           / path-rootless
  //           / path-empty
  private static final String HIER_PART =
      "(?://"
          + AUTHORITY
          + PATH_ABEMPTY
          + "|"
          + PATH_ABSOLUTE
          + "|"
          + PATH_ROOTLESS
          + "|"
          + PATH_EMPTY
          + ")";

  // relative-part = "//" authority path-abempty
  //               / path-absolute
  //               / path-noscheme
  //               / path-empty
  private static final String RELATIVE_PART =
      "(?://"
          + AUTHORITY
          + PATH_ABEMPTY
          + "|"
          + PATH_ABSOLUTE
          + "|"
          + PATH_NOSCHEME
          + "|"
          + PATH_EMPTY
          + ")";

  // ===========================================
  // Full URI and URI-reference
  // ===========================================

  // URI = scheme ":" hier-part [ "?" query ] [ "#" fragment ]
  private static final String URI =
      SCHEME + ":" + HIER_PART + "(?:\\?" + QUERY + ")?(?:#" + FRAGMENT + ")?";

  // relative-ref = relative-part [ "?" query ] [ "#" fragment ]
  private static final String RELATIVE_REF =
      RELATIVE_PART + "(?:\\?" + QUERY + ")?(?:#" + FRAGMENT + ")?";

  // URI-reference = URI / relative-ref
  private static final String URI_REFERENCE = "(?:" + URI + "|" + RELATIVE_REF + ")";

  // ===========================================
  // Compiled Patterns
  // ===========================================

  private static final Pattern URI_PATTERN = Pattern.compile("^" + URI + "$");
  private static final Pattern URI_REFERENCE_PATTERN = Pattern.compile("^" + URI_REFERENCE + "$");
  private static final Pattern RELATIVE_REF_PATTERN = Pattern.compile("^" + RELATIVE_REF + "$");

  // Component patterns for validation
  private static final Pattern SCHEME_PATTERN = Pattern.compile("^" + SCHEME + "$");
  private static final Pattern HOST_PATTERN = Pattern.compile("^" + HOST + "$");
  private static final Pattern IPV4_PATTERN = Pattern.compile("^" + IPV4_ADDRESS + "$");
  private static final Pattern IPV6_PATTERN = Pattern.compile("^" + IPV6_ADDRESS + "$");
  private static final Pattern PATH_PATTERN =
      Pattern.compile(
          "^(?:"
              + PATH_ABEMPTY
              + "|"
              + PATH_ABSOLUTE
              + "|"
              + PATH_ROOTLESS
              + "|"
              + PATH_NOSCHEME
              + ")$");
  private static final Pattern QUERY_PATTERN = Pattern.compile("^" + QUERY + "$");
  private static final Pattern FRAGMENT_PATTERN = Pattern.compile("^" + FRAGMENT + "$");

  // ===========================================
  // Public API
  // ===========================================

  /**
   * Validates a URI (must have a scheme).
   *
   * @param input the string to validate
   * @return true if valid RFC 3986 URI
   */
  public static boolean isValidUri(String input) {
    return URI_PATTERN.matcher(input).matches();
  }

  /**
   * Validates a URI-reference (URI or relative-ref). This is the most permissive validation.
   *
   * @param input the string to validate
   * @return true if valid RFC 3986 URI-reference
   */
  public static boolean isValidUriReference(String input) {
    return URI_REFERENCE_PATTERN.matcher(input).matches();
  }

  /**
   * Validates a relative reference (no scheme).
   *
   * @param input the string to validate
   * @return true if valid RFC 3986 relative-ref
   */
  public static boolean isValidRelativeRef(String input) {
    return RELATIVE_REF_PATTERN.matcher(input).matches();
  }

  /**
   * Validates a scheme component.
   *
   * @param input the scheme to validate
   * @return true if valid RFC 3986 scheme
   */
  public static boolean isValidScheme(String input) {
    return SCHEME_PATTERN.matcher(input).matches();
  }

  /**
   * Validates a host component.
   *
   * @param input the host to validate
   * @return true if valid RFC 3986 host
   */
  public static boolean isValidHost(String input) {
    return HOST_PATTERN.matcher(input).matches();
  }

  /**
   * Validates an IPv4 address.
   *
   * @param input the address to validate
   * @return true if valid IPv4 address
   */
  public static boolean isValidIpv4(String input) {
    return IPV4_PATTERN.matcher(input).matches();
  }

  /**
   * Validates an IPv6 address (without brackets).
   *
   * @param input the address to validate
   * @return true if valid IPv6 address
   */
  public static boolean isValidIpv6(String input) {
    return IPV6_PATTERN.matcher(input).matches();
  }

  /**
   * Validates a path component.
   *
   * @param input the path to validate
   * @return true if valid RFC 3986 path
   */
  public static boolean isValidPath(String input) {
    return PATH_PATTERN.matcher(input).matches();
  }

  /**
   * Validates a query component (without leading ?).
   *
   * @param input the query to validate
   * @return true if valid RFC 3986 query
   */
  public static boolean isValidQuery(String input) {
    return QUERY_PATTERN.matcher(input).matches();
  }

  /**
   * Validates a fragment component (without leading #).
   *
   * @param input the fragment to validate
   * @return true if valid RFC 3986 fragment
   */
  public static boolean isValidFragment(String input) {
    return FRAGMENT_PATTERN.matcher(input).matches();
  }

  /** Returns the regex pattern string for debugging/inspection. */
  public static String getUriPatternString() {
    return "^" + URI + "$";
  }

  /** Returns the regex pattern string for URI-reference for debugging/inspection. */
  public static String getUriReferencePatternString() {
    return "^" + URI_REFERENCE + "$";
  }
}
