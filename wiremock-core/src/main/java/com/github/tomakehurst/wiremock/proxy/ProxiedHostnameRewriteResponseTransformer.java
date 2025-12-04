/*
 * Copyright (C) 2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.proxy;

import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_LENGTH;

import com.github.tomakehurst.wiremock.common.ContentTypes;
import com.github.tomakehurst.wiremock.common.Gzip;
import com.github.tomakehurst.wiremock.extension.ResponseTransformerV2;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.wiremock.url.Authority;
import org.wiremock.url.Host;
import org.wiremock.url.Port;
import org.wiremock.url.Scheme;
import org.wiremock.url.Url;

/**
 * At the moment this transformer works with Proxy responses to replace the host and port. If we
 * decide to extend this to a generic host/port replacement we should rename the class and update
 * the transformer name
 */
public class ProxiedHostnameRewriteResponseTransformer implements ResponseTransformerV2 {

  @Override
  public Response transform(Response response, ServeEvent serveEvent) {

    if (!serveEvent.getResponseDefinition().isProxyResponse()) {
      return response;
    }

    var substitutionData = getSubstitutionData(serveEvent);

    // Update headers
    List<HttpHeader> updatedHeaderList =
        response.getHeaders().all().stream()
            .map(
                header ->
                    new HttpHeader(
                        header.key(),
                        header.values().stream()
                            .map(value -> applySubstitutions(value, substitutionData))
                            .toList()))
            .toList();
    HttpHeaders updatedHeaders = new HttpHeaders(updatedHeaderList);

    Response.Builder responseBuilder = Response.Builder.like(response).headers(updatedHeaders);

    // Update body if applicable
    byte[] initialBody = response.getBody();
    if (initialBody != null
        && initialBody.length > 0
        && ContentTypes.determineIsTextFromMimeType(getMimeType(response))) {
      byte[] updatedBody;
      if (Gzip.isGzipped(initialBody)) {
        String uncompressedBody = Gzip.unGzipToString(initialBody);
        uncompressedBody = applySubstitutions(uncompressedBody, substitutionData);
        updatedBody = Gzip.gzip(uncompressedBody.getBytes());
      } else {
        String responseBodyAsString = response.getBodyAsString();
        responseBodyAsString = applySubstitutions(responseBodyAsString, substitutionData);
        updatedBody = responseBodyAsString.getBytes();
      }
      responseBuilder.body(updatedBody);

      // Update Content-Length header if present
      if (updatedHeaders.getHeader(CONTENT_LENGTH).isPresent()) {
        responseBuilder.headers(
            setContentLengthHeader(updatedHeaders, String.valueOf(updatedBody.length)));
      }
    }

    return responseBuilder.build();
  }

  private static String applySubstitutions(String input, SubstitutionData data) {
    return data.combinedPattern.matcher(input).replaceAll(data::getReplacement);
  }

  private static class SubstitutionData {
    private final Pattern combinedPattern;
    private final Map<String, String> replacementMap;

    SubstitutionData(Map<Pattern, String> substitutions) {
      this.replacementMap = new LinkedHashMap<>();
      StringBuilder combinedPatternBuilder = new StringBuilder();
      int groupIndex = 0;

      for (Map.Entry<Pattern, String> entry : substitutions.entrySet()) {
        if (groupIndex > 0) {
          combinedPatternBuilder.append("|");
        }
        String patternStr = entry.getKey().pattern();
        combinedPatternBuilder.append("(").append(patternStr).append(")");
        replacementMap.put(String.valueOf(groupIndex), entry.getValue());
        groupIndex++;
      }

      this.combinedPattern =
          Pattern.compile(combinedPatternBuilder.toString(), Pattern.CASE_INSENSITIVE);
    }

    String getReplacement(java.util.regex.MatchResult matchResult) {
      for (int i = 1; i <= matchResult.groupCount(); i++) {
        if (matchResult.group(i) != null) {
          return replacementMap.get(String.valueOf(i - 1));
        }
      }
      return matchResult.group(0);
    }
  }

  @Override
  public String getName() {
    return "proxied-hostname-rewrite";
  }

  @Override
  public boolean applyGlobally() {
    return false;
  }

  private static HttpHeaders setContentLengthHeader(HttpHeaders headers, String value) {
    List<HttpHeader> filteredHeaders =
        headers.all().stream()
            .filter(h -> !h.keyEquals(CONTENT_LENGTH))
            .collect(Collectors.toList());
    filteredHeaders.add(new HttpHeader(CONTENT_LENGTH, value));
    return new HttpHeaders(filteredHeaders);
  }

  private static String getMimeType(Response response) {
    HttpHeaders responseHeaders = response.getHeaders();
    return responseHeaders != null && responseHeaders.getContentTypeHeader() != null
        ? responseHeaders.getContentTypeHeader().mimeTypePart()
        : null;
  }

  /**
   * This method can be used to pull substitution data from other sources such as stub metadata
   *
   * @param serveEvent the serveEvent for this request/response
   * @return The SubstitutionData containing combined pattern and replacements
   */
  private static SubstitutionData getSubstitutionData(ServeEvent serveEvent) {

    Url proxyUrl = Url.parse(serveEvent.getRequest().getAbsoluteUrl()).baseUrl();
    var proxyDefaultPort = proxyUrl.scheme().defaultPort();
    var proxyPort = proxyUrl.authority().port();
    var proxyActualPort = proxyPort == null ? proxyDefaultPort : proxyPort;
    var proxyWsScheme = getWebSocketScheme(proxyUrl);

    Url originUrl = Url.parse(serveEvent.getResponseDefinition().getProxyBaseUrl()).baseUrl();
    var originDefaultPort = originUrl.scheme().defaultPort();
    var originPort = originUrl.authority().port();
    var originActualPort = originPort == null ? originDefaultPort : originPort;
    var originWsScheme = getWebSocketScheme(originUrl);

    var replacements = new LinkedHashMap<Pattern, String>();

    if (Objects.equals(originActualPort, originDefaultPort)) {
      if (Objects.equals(proxyActualPort, proxyDefaultPort)) {
        // origin is on default port, proxy is on default port

        // https://origin.example.com:443 -> https://proxy.example.com:443
        replacements.put(
            fullUrlPattern(originUrl.scheme(), originUrl.host(), originActualPort),
            proxyUrl.withPort(proxyActualPort).toString());

        // https://origin.example.com -> https://proxy.example.com
        replacements.put(
            fullUrlPattern(originUrl.scheme(), originUrl.host()),
            proxyUrl.withoutPort().toString());

        // wss://origin.example.com:443 -> wss://proxy.example.com:443
        replacements.put(
            fullUrlPattern(originWsScheme, originUrl.host(), originActualPort),
            Url.builder(proxyWsScheme, Authority.of(proxyUrl.host(), proxyActualPort))
                .build()
                .toString());

        // wss://origin.example.com -> wss://proxy.example.com
        replacements.put(
            fullUrlPattern(originWsScheme, originUrl.host()),
            Url.builder(proxyWsScheme, Authority.of(proxyUrl.host())).build().toString());

        // origin.example.com:443 -> proxy.example.com:443
        replacements.put(
            schemelessPattern(originUrl.host(), originActualPort),
            proxyUrl.host() + ":" + proxyActualPort);

        // origin.example.com -> proxy.example.com
        replacements.put(schemelessPattern(originUrl.host()), proxyUrl.host().toString());

        // //origin.example.com -> //proxy.example.com
        replacements.put(
            pattern("[^:]", "//" + originUrl.host(), possibleHostPrefix), "//" + proxyUrl.host());
      } else {
        // origin is on default port, proxy is on custom port

        // https://origin.example.com:443 -> https://proxy.example.com:4434
        replacements.put(
            fullUrlPattern(originUrl.scheme(), originUrl.host(), originActualPort),
            proxyUrl.scheme() + "://" + proxyUrl.host() + ":" + proxyActualPort);

        // https://origin.example.com -> https://proxy.example.com:4434
        replacements.put(
            fullUrlPattern(originUrl.scheme(), originUrl.host()),
            proxyUrl.scheme() + "://" + proxyUrl.host() + ":" + proxyActualPort);

        // wss://origin.example.com:443 -> wss://proxy.example.com:4434
        replacements.put(
            fullUrlPattern(originWsScheme, originUrl.host(), originActualPort),
            proxyWsScheme + "://" + proxyUrl.host() + ":" + proxyActualPort);

        // wss://origin.example.com -> wss://proxy.example.com:4434
        replacements.put(
            fullUrlPattern(originWsScheme, originUrl.host()),
            proxyWsScheme + "://" + proxyUrl.host() + ":" + proxyActualPort);

        // origin.example.com:443 -> proxy.example.com:4434
        replacements.put(
            schemelessPattern(originUrl.host(), originActualPort),
            proxyUrl.host() + ":" + proxyActualPort);

        // origin.example.com -> proxy.example.com:4434
        replacements.put(
            schemelessPattern(originUrl.host()), proxyUrl.host() + ":" + proxyActualPort);

        // //origin.example.com -> https://proxy.example.com:4434
        replacements.put(
            pattern("[^:]", "//" + originUrl.host(), possibleHostPrefix),
            proxyUrl.scheme() + "://" + proxyUrl.host() + ":" + proxyActualPort);
      }
    } else {
      if (Objects.equals(proxyActualPort, proxyDefaultPort)) {
        // origin is on custom port, proxy is on default port

        // https://origin.example.com:4434 -> https://proxy.example.com
        replacements.put(
            fullUrlPattern(originUrl.scheme(), originUrl.host(), originActualPort),
            proxyUrl.scheme() + "://" + proxyUrl.host());

        // wss://origin.example.com:4434 -> wss://proxy.example.com
        replacements.put(
            fullUrlPattern(originWsScheme, originUrl.host(), originActualPort),
            proxyWsScheme + "://" + proxyUrl.host());

        // origin.example.com:4434 -> proxy.example.com
        replacements.put(
            schemelessPattern(originUrl.host(), originActualPort), proxyUrl.host().toString());

        // origin.example.com -> proxy.example.com
        replacements.put(schemelessPattern(originUrl.host()), proxyUrl.host().toString());
      } else {
        // origin is on custom port, proxy is on custom port

        // https://origin.example.com:4434 -> https://proxy.example.com:4434
        replacements.put(
            fullUrlPattern(originUrl.scheme(), originUrl.host(), originActualPort),
            proxyUrl.scheme() + "://" + proxyUrl.host() + ":" + proxyActualPort);

        // wss://origin.example.com:4434 -> wss://proxy.example.com:4434
        replacements.put(
            fullUrlPattern(originWsScheme, originUrl.host(), originActualPort),
            proxyWsScheme + "://" + proxyUrl.host() + ":" + proxyActualPort);

        // origin.example.com:4434 -> proxy.example.com:4434
        replacements.put(
            schemelessPattern(originUrl.host(), originActualPort),
            proxyUrl.host() + ":" + proxyActualPort);

        // origin.example.com -> proxy.example.com
        replacements.put(schemelessPattern(originUrl.host()), proxyUrl.host().toString());
      }
    }

    return new SubstitutionData(replacements);
  }

  // language=RegExp
  private static final String possibleSchemePrefixes = "[^a-zA-Z0-9+-.]";
  // language=RegExp
  private static final String possibleHostPrefix = "[^a-zA-Z0-9-.!$&'()*+,;=:/]";
  // language=RegExp
  private static final String possibleHostPostfix = "[^a-zA-Z0-9-.!$&'()*+,;=:]";
  // language RegExp
  private static final String possiblePortPostfix = "\\D";

  private static Pattern fullUrlPattern(Scheme scheme, Host host) {
    return pattern(possibleSchemePrefixes, scheme + "://" + host, possibleHostPostfix);
  }

  private static Pattern fullUrlPattern(Scheme scheme, Host host, Port port) {
    return pattern(possibleSchemePrefixes, scheme + "://" + host + ":" + port, possiblePortPostfix);
  }

  private static Pattern schemelessPattern(Host host) {
    return pattern(possibleHostPrefix, host.toString(), possibleHostPostfix);
  }

  private static Pattern schemelessPattern(Host host, Port port) {
    return pattern(possibleHostPrefix, host + ":" + port, possiblePortPostfix);
  }

  private static Pattern pattern(String prefixPattern, String literal, String postfixPattern) {
    return Pattern.compile(
        "(?<=" + prefixPattern + "|^)" + Pattern.quote(literal) + "(?=" + postfixPattern + "|$)",
        Pattern.CASE_INSENSITIVE);
  }

  private static Scheme getWebSocketScheme(Url proxyUrl) {
    return proxyUrl.scheme().equals(Scheme.https) ? Scheme.wss : Scheme.ws;
  }
}
