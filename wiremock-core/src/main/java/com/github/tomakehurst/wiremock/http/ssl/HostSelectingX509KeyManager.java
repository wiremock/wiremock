/*
 * Copyright (C) 2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http.ssl;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;

import java.net.Socket;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509ExtendedKeyManager;

/**
 * Picks the client certificate to present by the host being connected to.
 *
 * <p>The JDK key manager chooses a client key only from the server's CertificateRequest (accepted
 * key types and, if sent, accepted CA names). When the trust store holds several client keys and a
 * server sends no CA names, it presents whichever key matches the first accepted key type, which
 * can be the wrong one. This key manager consults an explicit host-to-alias mapping first and falls
 * back to the default selection for hosts that are not mapped.
 *
 * <p>Host keys are matched case-insensitively, either exactly ({@code api.example.com}) or as a
 * wildcard suffix ({@code *.example.com}, matching any subdomain). Exact matches win.
 */
public class HostSelectingX509KeyManager extends DelegatingX509ExtendedKeyManager {

  private final X509ExtendedKeyManager delegate;
  private final Map<String, String> aliasesByHost;

  public HostSelectingX509KeyManager(
      X509ExtendedKeyManager delegate, Map<String, String> aliasesByHost) {
    super(delegate);
    this.delegate = delegate;
    this.aliasesByHost = normalise(aliasesByHost);
  }

  @Override
  public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
    String alias = mappedAlias(keyType, peerHost(socket));
    return alias != null ? alias : delegate.chooseClientAlias(keyType, issuers, socket);
  }

  @Override
  public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
    String alias = mappedAlias(keyType, engine != null ? engine.getPeerHost() : null);
    return alias != null ? alias : delegate.chooseEngineClientAlias(keyType, issuers, engine);
  }

  /** The configured alias for {@code host}, or null when the host is not mapped. */
  String configuredAliasFor(String host) {
    if (host == null) {
      return null;
    }
    String h = host.toLowerCase(Locale.ROOT);
    String exact = aliasesByHost.get(h);
    if (exact != null) {
      return exact;
    }
    String best = null;
    int bestLength = -1;
    for (Map.Entry<String, String> entry : aliasesByHost.entrySet()) {
      String key = entry.getKey();
      if (key.startsWith("*.")) {
        String suffix = key.substring(1); // ".example.com"
        if (h.endsWith(suffix) && suffix.length() > bestLength) {
          best = entry.getValue();
          bestLength = suffix.length();
        }
      }
    }
    return best;
  }

  private String mappedAlias(String[] keyTypes, String host) {
    String configured = configuredAliasFor(host);
    if (configured == null) {
      return null;
    }
    String resolved = resolveAlias(configured, keyTypes);
    if (resolved == null) {
      notifier()
          .error(
              "Client certificate alias '"
                  + configured
                  + "' configured for host '"
                  + host
                  + "' is not a private key entry usable for the key types requested by the"
                  + " server; falling back to default client certificate selection");
      return null;
    }
    notifier().info("Using client certificate '" + configured + "' for host '" + host + "'");
    return resolved;
  }

  /**
   * Finds the key manager's own name for the configured alias among the keys usable for the
   * requested key types. Key stores report aliases in lower case (PKCS12), and some key manager
   * implementations prefix them (e.g. "1.0.alias"), so the match is case-insensitive and also
   * accepts a "." prefix.
   */
  private String resolveAlias(String configured, String[] keyTypes) {
    if (keyTypes == null) {
      return null;
    }
    String wanted = configured.toLowerCase(Locale.ROOT);
    for (String keyType : keyTypes) {
      String[] candidates = delegate.getClientAliases(keyType, null);
      if (candidates == null) {
        continue;
      }
      for (String candidate : candidates) {
        String c = candidate.toLowerCase(Locale.ROOT);
        if (c.equals(wanted) || c.endsWith("." + wanted)) {
          return candidate;
        }
      }
    }
    return null;
  }

  private static String peerHost(Socket socket) {
    if (socket instanceof SSLSocket sslSocket) {
      SSLSession session = sslSocket.getHandshakeSession();
      if (session != null) {
        return session.getPeerHost();
      }
    }
    return null;
  }

  private static Map<String, String> normalise(Map<String, String> aliasesByHost) {
    Map<String, String> normalised = new LinkedHashMap<>();
    if (aliasesByHost != null) {
      aliasesByHost.forEach(
          (host, alias) -> normalised.put(host.trim().toLowerCase(Locale.ROOT), alias.trim()));
    }
    return normalised;
  }
}
