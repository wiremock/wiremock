/*
 * Copyright (C) 2011-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.standalone;

import static com.github.tomakehurst.wiremock.common.BrowserProxySettings.DEFAULT_CA_KESTORE_PASSWORD;
import static com.github.tomakehurst.wiremock.common.BrowserProxySettings.DEFAULT_CA_KEYSTORE_PATH;
import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.common.ProxySettings.NO_PROXY;
import static com.github.tomakehurst.wiremock.common.ResourceUtil.getResource;
import static com.github.tomakehurst.wiremock.core.WireMockApp.MAPPINGS_ROOT;
import static com.github.tomakehurst.wiremock.http.CaseInsensitiveKey.TO_CASE_INSENSITIVE_KEYS;

import com.github.tomakehurst.wiremock.common.*;
import com.github.tomakehurst.wiremock.common.filemaker.FilenameMaker;
import com.github.tomakehurst.wiremock.common.ssl.KeyStoreSettings;
import com.github.tomakehurst.wiremock.common.ssl.KeyStoreSourceFactory;
import com.github.tomakehurst.wiremock.core.MappingsSaver;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.extension.ExtensionDeclarations;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.http.CaseInsensitiveKey;
import com.github.tomakehurst.wiremock.http.HttpServerFactory;
import com.github.tomakehurst.wiremock.http.ThreadPoolFactory;
import com.github.tomakehurst.wiremock.http.trafficlistener.ConsoleNotifyingWiremockNetworkTrafficListener;
import com.github.tomakehurst.wiremock.http.trafficlistener.DoNothingWiremockNetworkTrafficListener;
import com.github.tomakehurst.wiremock.http.trafficlistener.WiremockNetworkTrafficListener;
import com.github.tomakehurst.wiremock.jetty.QueuedThreadPoolFactory;
import com.github.tomakehurst.wiremock.security.Authenticator;
import com.github.tomakehurst.wiremock.security.BasicAuthenticator;
import com.github.tomakehurst.wiremock.security.NoAuthenticator;
import com.github.tomakehurst.wiremock.store.DefaultStores;
import com.github.tomakehurst.wiremock.store.Stores;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.*;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class CommandLineOptions implements Options {

  private static final String HELP = "help";
  private static final String RECORD_MAPPINGS = "record-mappings";
  private static final String MATCH_HEADERS = "match-headers";
  private static final String PROXY_ALL = "proxy-all";
  private static final String PRESERVE_HOST_HEADER = "preserve-host-header";
  private static final String PROXY_VIA = "proxy-via";
  private static final String TIMEOUT = "timeout";
  private static final String PORT = "port";
  private static final String DISABLE_HTTP = "disable-http";
  private static final String BIND_ADDRESS = "bind-address";
  private static final String HTTPS_PORT = "https-port";
  private static final String HTTPS_KEYSTORE = "https-keystore";
  private static final String HTTPS_KEYSTORE_PASSWORD = "keystore-password";
  private static final String HTTPS_KEYSTORE_TYPE = "keystore-type";
  private static final String HTTPS_KEY_MANAGER_PASSWORD = "key-manager-password";
  private static final String HTTPS_TRUSTSTORE = "https-truststore";
  private static final String HTTPS_TRUSTSTORE_PASSWORD = "truststore-password";
  private static final String HTTPS_TRUSTSTORE_TYPE = "truststore-type";
  private static final String REQUIRE_CLIENT_CERT = "https-require-client-cert";
  private static final String VERBOSE = "verbose";
  private static final String ENABLE_BROWSER_PROXYING = "enable-browser-proxying";
  private static final String DISABLE_BANNER = "disable-banner";
  private static final String DISABLE_REQUEST_JOURNAL = "no-request-journal";
  private static final String EXTENSIONS = "extensions";
  private static final String MAX_ENTRIES_REQUEST_JOURNAL = "max-request-journal-entries";
  private static final String JETTY_ACCEPTOR_THREAD_COUNT = "jetty-acceptor-threads";
  private static final String PRINT_ALL_NETWORK_TRAFFIC = "print-all-network-traffic";
  private static final String JETTY_ACCEPT_QUEUE_SIZE = "jetty-accept-queue-size";
  @Deprecated private static final String JETTY_HEADER_BUFFER_SIZE = "jetty-header-buffer-size";
  private static final String JETTY_HEADER_REQUEST_SIZE = "jetty-header-request-size";
  private static final String JETTY_HEADER_RESPONSE_SIZE = "jetty-header-response-size";
  private static final String JETTY_STOP_TIMEOUT = "jetty-stop-timeout";
  private static final String JETTY_IDLE_TIMEOUT = "jetty-idle-timeout";
  private static final String ROOT_DIR = "root-dir";
  private static final String CONTAINER_THREADS = "container-threads";
  private static final String GLOBAL_RESPONSE_TEMPLATING = "global-response-templating";
  private static final String LOCAL_RESPONSE_TEMPLATING = "local-response-templating";
  private static final String DISABLE_RESPONSE_TEMPLATING = "disable-response-templating";
  public static final String FILENAME_TEMPLATE = "filename-template";
  private static final String ADMIN_API_BASIC_AUTH = "admin-api-basic-auth";
  private static final String ADMIN_API_REQUIRE_HTTPS = "admin-api-require-https";
  private static final String ASYNCHRONOUS_RESPONSE_ENABLED = "async-response-enabled";
  private static final String ASYNCHRONOUS_RESPONSE_THREADS = "async-response-threads";
  private static final String USE_CHUNKED_ENCODING = "use-chunked-encoding";
  private static final String MAX_TEMPLATE_CACHE_ENTRIES = "max-template-cache-entries";
  private static final String PERMITTED_SYSTEM_KEYS = "permitted-system-keys";
  private static final String DISABLE_GZIP = "disable-gzip";
  private static final String DISABLE_REQUEST_LOGGING = "disable-request-logging";
  private static final String ENABLE_STUB_CORS = "enable-stub-cors";
  private static final String TRUST_ALL_PROXY_TARGETS = "trust-all-proxy-targets";
  private static final String TRUST_PROXY_TARGET = "trust-proxy-target";
  private static final String HTTPS_CA_KEYSTORE = "ca-keystore";
  private static final String HTTPS_CA_KEYSTORE_PASSWORD = "ca-keystore-password";
  private static final String HTTPS_CA_KEYSTORE_TYPE = "ca-keystore-type";
  private static final String DISABLE_OPTIMIZE_XML_FACTORIES_LOADING =
      "disable-optimize-xml-factories-loading";
  private static final String DISABLE_STRICT_HTTP_HEADERS = "disable-strict-http-headers";
  private static final String LOAD_RESOURCES_FROM_CLASSPATH = "load-resources-from-classpath";
  private static final String LOGGED_RESPONSE_BODY_SIZE_LIMIT = "logged-response-body-size-limit";
  private static final String ALLOW_PROXY_TARGETS = "allow-proxy-targets";
  private static final String DENY_PROXY_TARGETS = "deny-proxy-targets";
  private static final String PROXY_TIMEOUT = "proxy-timeout";

  private static final String PROXY_PASS_THROUGH = "proxy-pass-through";

  private final OptionSet optionSet;

  private final Stores stores;
  private final FileSource fileSource;

  private final MappingsSource mappingsSource;
  private final ExtensionDeclarations extensions;
  private final FilenameMaker filenameMaker;

  private String helpText;
  private Integer actualHttpPort;
  private Integer actualHttpsPort;

  public CommandLineOptions(String... args) {
    OptionParser optionParser = new OptionParser();
    optionParser
        .accepts(
            PORT,
            "The port number for the server to listen on (default: 8080). 0 for dynamic port selection.")
        .withRequiredArg();
    optionParser.accepts(DISABLE_HTTP, "Disable the default HTTP listener.");
    optionParser
        .accepts(
            HTTPS_PORT,
            "If this option is present WireMock will enable HTTPS on the specified port")
        .withRequiredArg();
    optionParser.accepts(BIND_ADDRESS, "The IP to listen connections").withRequiredArg();
    optionParser.accepts(CONTAINER_THREADS, "The number of container threads").withRequiredArg();
    optionParser.accepts(TIMEOUT, "The default global timeout.");
    optionParser.accepts(
        DISABLE_OPTIMIZE_XML_FACTORIES_LOADING,
        "Whether to disable optimize XML factories loading or not.");
    optionParser.accepts(
        DISABLE_STRICT_HTTP_HEADERS,
        "Whether to disable strict HTTP header handling of Jetty or not.");
    optionParser.accepts(
        REQUIRE_CLIENT_CERT,
        "Make the server require a trusted client certificate to enable a connection");
    optionParser
        .accepts(HTTPS_TRUSTSTORE_TYPE, "The HTTPS trust store type")
        .withRequiredArg()
        .defaultsTo("JKS");
    optionParser
        .accepts(HTTPS_TRUSTSTORE_PASSWORD, "Password for the trust store")
        .withRequiredArg()
        .defaultsTo("password");
    optionParser
        .accepts(
            HTTPS_TRUSTSTORE,
            "Path to an alternative truststore for HTTPS client certificates. Must have a password of \"password\".")
        .requiredIf(REQUIRE_CLIENT_CERT)
        .requiredIf(HTTPS_TRUSTSTORE_PASSWORD)
        .withRequiredArg();
    optionParser
        .accepts(HTTPS_KEYSTORE_TYPE, "The HTTPS keystore type.")
        .withRequiredArg()
        .defaultsTo("JKS");
    optionParser
        .accepts(HTTPS_KEYSTORE_PASSWORD, "Password for the alternative keystore.")
        .withRequiredArg()
        .defaultsTo("password");
    optionParser
        .accepts(
            HTTPS_KEY_MANAGER_PASSWORD,
            "Key manager password for use with the alternative keystore.")
        .withRequiredArg()
        .defaultsTo("password");
    optionParser
        .accepts(
            HTTPS_KEYSTORE,
            "Path to an alternative keystore for HTTPS. Password is assumed to be \"password\" if not specified.")
        .requiredIf(HTTPS_KEYSTORE_PASSWORD)
        .withRequiredArg()
        .defaultsTo(getResource(CommandLineOptions.class, "keystore").toString());
    optionParser
        .accepts(PROXY_ALL, "Will create a proxy mapping for /* to the specified URL")
        .withRequiredArg();
    optionParser.accepts(
        PRESERVE_HOST_HEADER,
        "Will transfer the original host header from the client to the proxied service");
    optionParser
        .accepts(PROXY_VIA, "Specifies a proxy server to use when routing proxy mapped requests")
        .withRequiredArg();
    optionParser.accepts(
        RECORD_MAPPINGS, "Enable recording of all (non-admin) requests as mapping files");
    optionParser
        .accepts(MATCH_HEADERS, "Enable request header matching when recording through a proxy")
        .withRequiredArg();
    optionParser
        .accepts(
            ROOT_DIR,
            "Specifies path for storing recordings (parent for "
                + MAPPINGS_ROOT
                + " and "
                + WireMockApp.FILES_ROOT
                + " folders)")
        .withRequiredArg()
        .defaultsTo(".");
    optionParser.accepts(VERBOSE, "Enable verbose logging to stdout");
    optionParser.accepts(
        ENABLE_BROWSER_PROXYING, "Allow wiremock to be set as a browser's proxy server");
    optionParser.accepts(
        DISABLE_REQUEST_JOURNAL,
        "Disable the request journal (to avoid heap growth when running wiremock for long periods without reset)");
    optionParser.accepts(DISABLE_BANNER, "Disable print banner logo");
    optionParser
        .accepts(
            EXTENSIONS,
            "Matching and/or response transformer extension class names, comma separated.")
        .withRequiredArg();
    optionParser
        .accepts(
            MAX_ENTRIES_REQUEST_JOURNAL,
            "Set maximum number of entries in request journal (if enabled) to discard old entries if the log becomes too large. Default: no discard")
        .withRequiredArg();
    optionParser
        .accepts(JETTY_ACCEPTOR_THREAD_COUNT, "Number of Jetty acceptor threads")
        .withRequiredArg();
    optionParser
        .accepts(JETTY_ACCEPT_QUEUE_SIZE, "The size of Jetty's accept queue size")
        .withRequiredArg();
    optionParser
        .accepts(
            JETTY_HEADER_BUFFER_SIZE, "Deprecated. The size of Jetty's buffer for request headers")
        .withRequiredArg();
    optionParser
        .accepts(JETTY_HEADER_REQUEST_SIZE, "The size of Jetty's buffer for request headers")
        .withRequiredArg();
    optionParser
        .accepts(JETTY_HEADER_RESPONSE_SIZE, "The size of Jetty's buffer for response headers")
        .withRequiredArg();
    optionParser
        .accepts(JETTY_STOP_TIMEOUT, "Timeout in milliseconds for Jetty to stop")
        .withRequiredArg();
    optionParser
        .accepts(JETTY_IDLE_TIMEOUT, "Idle timeout in milliseconds for Jetty connections")
        .withRequiredArg();
    optionParser.accepts(
        PRINT_ALL_NETWORK_TRAFFIC,
        "Print all raw incoming and outgoing network traffic to console");
    optionParser.accepts(
        GLOBAL_RESPONSE_TEMPLATING, "Preprocess all responses with Handlebars templates");
    optionParser.accepts(FILENAME_TEMPLATE, "Add filename template").withRequiredArg();
    optionParser.accepts(
        LOCAL_RESPONSE_TEMPLATING, "Preprocess selected responses with Handlebars templates");
    optionParser.accepts(
        DISABLE_RESPONSE_TEMPLATING, "Disable processing of responses with Handlebars templates");
    optionParser
        .accepts(
            ADMIN_API_BASIC_AUTH,
            "Require HTTP Basic authentication for admin API calls with the supplied credentials in username:password format")
        .withRequiredArg();
    optionParser.accepts(
        ADMIN_API_REQUIRE_HTTPS, "Require HTTPS to be used to access the admin API");
    optionParser
        .accepts(ASYNCHRONOUS_RESPONSE_ENABLED, "Enable asynchronous response")
        .withRequiredArg()
        .defaultsTo("false");
    optionParser
        .accepts(ASYNCHRONOUS_RESPONSE_THREADS, "Number of asynchronous response threads")
        .withRequiredArg()
        .defaultsTo("10");
    optionParser
        .accepts(
            USE_CHUNKED_ENCODING,
            "Whether to use Transfer-Encoding: chunked in responses. Can be set to always, never or body_file.")
        .withRequiredArg()
        .defaultsTo("always");
    optionParser
        .accepts(
            MAX_TEMPLATE_CACHE_ENTRIES,
            "The maximum number of response template fragments that can be cached. Only has any effect when templating is enabled. Defaults to no limit.")
        .withOptionalArg();
    optionParser
        .accepts(
            PERMITTED_SYSTEM_KEYS,
            "A list of case-insensitive regular expressions for names of permitted system properties and environment vars. Only has any effect when templating is enabled. Defaults to no limit.")
        .withOptionalArg()
        .ofType(String.class)
        .withValuesSeparatedBy(",");
    optionParser.accepts(DISABLE_GZIP, "Disable gzipping of request and response bodies");
    optionParser.accepts(
        DISABLE_REQUEST_LOGGING,
        "Disable logging of stub requests and responses to the notifier. Useful when performance testing.");
    optionParser.accepts(
        ENABLE_STUB_CORS, "Enable automatic sending of CORS headers with stub responses.");
    optionParser
        .accepts(
            TRUST_ALL_PROXY_TARGETS,
            "Trust all certificates presented by origins when browser proxying")
        .availableIf(ENABLE_BROWSER_PROXYING);
    optionParser
        .accepts(
            TRUST_PROXY_TARGET,
            "Trust any certificate presented by this origin when browser proxying")
        .availableIf(ENABLE_BROWSER_PROXYING)
        .availableUnless(TRUST_ALL_PROXY_TARGETS)
        .withRequiredArg();
    optionParser
        .accepts(
            HTTPS_CA_KEYSTORE,
            "Path to an alternative keystore containing a Certificate Authority private key & certificate for generating certificates when proxying HTTPS. Password is assumed to be \"password\" if not specified.")
        .availableIf(ENABLE_BROWSER_PROXYING)
        .withRequiredArg()
        .defaultsTo(DEFAULT_CA_KEYSTORE_PATH);
    optionParser
        .accepts(HTTPS_CA_KEYSTORE_PASSWORD, "Password for the alternative CA keystore.")
        .availableIf(HTTPS_CA_KEYSTORE)
        .withRequiredArg()
        .defaultsTo(DEFAULT_CA_KESTORE_PASSWORD);
    optionParser
        .accepts(HTTPS_CA_KEYSTORE_TYPE, "Type of the alternative CA keystore (jks or pkcs12).")
        .availableIf(HTTPS_CA_KEYSTORE)
        .withRequiredArg()
        .defaultsTo("jks");
    optionParser
        .accepts(
            LOAD_RESOURCES_FROM_CLASSPATH,
            "Specifies path on the classpath for storing recordings (parent for "
                + MAPPINGS_ROOT
                + " and "
                + WireMockApp.FILES_ROOT
                + " folders)")
        .withRequiredArg();
    optionParser
        .accepts(
            LOGGED_RESPONSE_BODY_SIZE_LIMIT,
            "Maximum size for response bodies stored in the request journal beyond which truncation will be applied")
        .withRequiredArg();
    optionParser
        .accepts(
            ALLOW_PROXY_TARGETS,
            "Comma separated list of IP addresses, IP ranges (hyphenated) and domain name wildcards that can be proxied to/recorded from. Is evaluated before the list of denied addresses.")
        .withRequiredArg();
    optionParser
        .accepts(
            DENY_PROXY_TARGETS,
            "Comma separated list of IP addresses, IP ranges (hyphenated) and domain name wildcards that cannot be proxied to/recorded from. Is evaluated after the list of allowed addresses.")
        .withRequiredArg();
    optionParser
        .accepts(PROXY_TIMEOUT, "Timeout in milliseconds for requests to proxy")
        .withRequiredArg();
    optionParser
        .accepts(PROXY_PASS_THROUGH, "Flag to control browser proxy pass through")
        .withRequiredArg();

    optionParser.accepts(HELP, "Print this message").forHelp();

    optionSet = optionParser.parse(args);
    validate();
    captureHelpTextIfRequested(optionParser);

    extensions = new ExtensionDeclarations();

    if (optionSet.has(LOAD_RESOURCES_FROM_CLASSPATH)) {
      fileSource =
          new ClasspathFileSource((String) optionSet.valueOf(LOAD_RESOURCES_FROM_CLASSPATH));
    } else {
      fileSource = new SingleRootFileSource((String) optionSet.valueOf(ROOT_DIR));
    }

    stores = new DefaultStores(fileSource);

    if (optionSet.has(PROXY_PASS_THROUGH)) {
      GlobalSettings newSettings =
          stores
              .getSettingsStore()
              .get()
              .copy()
              .proxyPassThrough(
                  Boolean.parseBoolean((String) optionSet.valueOf(PROXY_PASS_THROUGH)))
              .build();
      stores.getSettingsStore().set(newSettings);
    }

    filenameMaker = new FilenameMaker(getFilenameTemplateOption());
    mappingsSource = new JsonFileMappingsSource(fileSource.child(MAPPINGS_ROOT), filenameMaker);
    buildExtensions();

    actualHttpPort = null;
  }

  private void buildExtensions() {
    if (optionSet.has(EXTENSIONS)) {
      String classNamesParamValue = (String) optionSet.valueOf(EXTENSIONS);
      final String[] classNames = classNamesParamValue.split(",");
      extensions.add(classNames);
    }
  }

  private String getFilenameTemplateOption() {
    if (optionSet.has(FILENAME_TEMPLATE)) {
      String filenameTemplate = (String) optionSet.valueOf(FILENAME_TEMPLATE);
      validateFilenameTemplate(filenameTemplate);
      return filenameTemplate;
    }

    return null;
  }

  private void validateFilenameTemplate(String filenameTemplate) {
    String[] templateParts = filenameTemplate.split("-");
    boolean handlebarIdentifierMissed =
        Arrays.stream(templateParts)
            .anyMatch(part -> !part.contains("{{{") || !part.contains("}}}"));
    if (handlebarIdentifierMissed) {
      throw new IllegalArgumentException(
          "Format for filename template should be contain handlebar value. Please check format one more time");
    }
  }

  private void validate() {
    if (optionSet.has(PORT) && optionSet.has(DISABLE_HTTP)) {
      throw new IllegalArgumentException(
          "The HTTP listener can't have a port set and be disabled at the same time");
    }
    if (!optionSet.has(HTTPS_PORT) && optionSet.has(DISABLE_HTTP)) {
      throw new IllegalArgumentException("HTTPS must be enabled if HTTP is not.");
    }

    if (optionSet.has(RECORD_MAPPINGS) && optionSet.has(DISABLE_REQUEST_JOURNAL)) {
      throw new IllegalArgumentException("Request journal must be enabled to record stubs");
    }
  }

  private void captureHelpTextIfRequested(OptionParser optionParser) {
    if (optionSet.has(HELP)) {
      StringWriter out = new StringWriter();
      try {
        optionParser.printHelpOn(out);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      helpText = out.toString();
    }
  }

  public boolean verboseLoggingEnabled() {
    return optionSet.has(VERBOSE);
  }

  public boolean recordMappingsEnabled() {
    return optionSet.has(RECORD_MAPPINGS);
  }

  @Override
  public List<CaseInsensitiveKey> matchingHeaders() {
    if (optionSet.hasArgument(MATCH_HEADERS)) {
      String headerSpec = (String) optionSet.valueOf(MATCH_HEADERS);

      return Arrays.stream(headerSpec.split(","))
          .map(TO_CASE_INSENSITIVE_KEYS)
          .collect(Collectors.toUnmodifiableList());
    }

    return Collections.emptyList();
  }

  @Override
  public HttpServerFactory httpServerFactory() {
    try {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class<?> cls =
          loader.loadClass("com.github.tomakehurst.wiremock.jetty.JettyHttpServerFactory");
      return (HttpServerFactory) cls.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      return throwUnchecked(e, null);
    }
  }

  @Override
  public ThreadPoolFactory threadPoolFactory() {
    return new QueuedThreadPoolFactory();
  }

  private boolean specifiesPortNumber() {
    return optionSet.has(PORT);
  }

  @Override
  public int portNumber() {
    if (specifiesPortNumber()) {
      return Integer.parseInt((String) optionSet.valueOf(PORT));
    }

    return DEFAULT_PORT;
  }

  @Override
  public boolean getHttpDisabled() {
    return optionSet.has(DISABLE_HTTP);
  }

  public void setActualHttpPort(int port) {
    actualHttpPort = port;
  }

  public void setActualHttpsPort(int port) {
    actualHttpsPort = port;
  }

  @Override
  public String bindAddress() {
    if (optionSet.has(BIND_ADDRESS)) {
      return (String) optionSet.valueOf(BIND_ADDRESS);
    }

    return DEFAULT_BIND_ADDRESS;
  }

  @Override
  public FilenameMaker getFilenameMaker() {
    return filenameMaker;
  }

  @Override
  public HttpsSettings httpsSettings() {
    return new HttpsSettings.Builder()
        .port(httpsPortNumber())
        .keyStorePath((String) optionSet.valueOf(HTTPS_KEYSTORE))
        .keyStorePassword((String) optionSet.valueOf(HTTPS_KEYSTORE_PASSWORD))
        .keyStoreType((String) optionSet.valueOf(HTTPS_KEYSTORE_TYPE))
        .keyManagerPassword((String) optionSet.valueOf(HTTPS_KEY_MANAGER_PASSWORD))
        .trustStorePath((String) optionSet.valueOf(HTTPS_TRUSTSTORE))
        .trustStorePassword((String) optionSet.valueOf(HTTPS_TRUSTSTORE_PASSWORD))
        .trustStoreType((String) optionSet.valueOf(HTTPS_TRUSTSTORE_TYPE))
        .needClientAuth(optionSet.has(REQUIRE_CLIENT_CERT))
        .build();
  }

  @Override
  public JettySettings jettySettings() {

    JettySettings.Builder builder = JettySettings.Builder.aJettySettings();

    if (optionSet.hasArgument(JETTY_ACCEPTOR_THREAD_COUNT)) {
      builder =
          builder.withAcceptors(
              Integer.parseInt((String) optionSet.valueOf(JETTY_ACCEPTOR_THREAD_COUNT)));
    }

    if (optionSet.hasArgument(JETTY_ACCEPT_QUEUE_SIZE)) {
      builder =
          builder.withAcceptQueueSize(
              Integer.parseInt((String) optionSet.valueOf(JETTY_ACCEPT_QUEUE_SIZE)));
    }

    // @Deprecated
    if (optionSet.hasArgument(JETTY_HEADER_BUFFER_SIZE)) {
      builder =
          builder.withRequestHeaderSize(
              Integer.parseInt((String) optionSet.valueOf(JETTY_HEADER_BUFFER_SIZE)));
    }

    if (optionSet.hasArgument(JETTY_HEADER_REQUEST_SIZE)) {
      builder =
          builder.withRequestHeaderSize(
              Integer.parseInt((String) optionSet.valueOf(JETTY_HEADER_REQUEST_SIZE)));
    }

    if (optionSet.hasArgument(JETTY_HEADER_RESPONSE_SIZE)) {
      builder =
          builder.withResponseHeaderSize(
              Integer.parseInt((String) optionSet.valueOf(JETTY_HEADER_RESPONSE_SIZE)));
    }

    if (optionSet.hasArgument(JETTY_STOP_TIMEOUT)) {
      builder =
          builder.withStopTimeout(Long.parseLong((String) optionSet.valueOf(JETTY_STOP_TIMEOUT)));
    }

    if (optionSet.hasArgument(JETTY_IDLE_TIMEOUT)) {
      builder =
          builder.withIdleTimeout(Long.parseLong((String) optionSet.valueOf(JETTY_IDLE_TIMEOUT)));
    }

    return builder.build();
  }

  private int httpsPortNumber() {
    return optionSet.has(HTTPS_PORT)
        ? Integer.parseInt((String) optionSet.valueOf(HTTPS_PORT))
        : -1;
  }

  public boolean help() {
    return optionSet.has(HELP);
  }

  public String helpText() {
    return helpText;
  }

  public boolean specifiesProxyUrl() {
    return optionSet.has(PROXY_ALL);
  }

  public String proxyUrl() {
    return (String) optionSet.valueOf(PROXY_ALL);
  }

  @Override
  public boolean shouldPreserveHostHeader() {
    return optionSet.has(PRESERVE_HOST_HEADER);
  }

  @Override
  public String proxyHostHeader() {
    return optionSet.hasArgument(PROXY_ALL)
        ? URI.create((String) optionSet.valueOf(PROXY_ALL)).getAuthority()
        : null;
  }

  @Override
  public ExtensionDeclarations getDeclaredExtensions() {
    return extensions;
  }

  @Override
  public WiremockNetworkTrafficListener networkTrafficListener() {
    if (optionSet.has(PRINT_ALL_NETWORK_TRAFFIC)) {
      return new ConsoleNotifyingWiremockNetworkTrafficListener();
    } else {
      return new DoNothingWiremockNetworkTrafficListener();
    }
  }

  @Override
  public Authenticator getAdminAuthenticator() {
    if (optionSet.has(ADMIN_API_BASIC_AUTH)) {
      String[] parts = ((String) optionSet.valueOf(ADMIN_API_BASIC_AUTH)).split(":");
      if (parts.length != 2) {
        throw new IllegalArgumentException(
            "Admin API credentials must be in the format username:password");
      }

      return new BasicAuthenticator(parts[0], parts[1]);
    }

    return new NoAuthenticator();
  }

  @Override
  public boolean getHttpsRequiredForAdminApi() {
    return optionSet.has(ADMIN_API_REQUIRE_HTTPS);
  }

  /**
   * @deprecated use {@link BrowserProxySettings#enabled()}
   */
  @Deprecated
  @Override
  public boolean browserProxyingEnabled() {
    return browserProxySettings().enabled();
  }

  @Override
  public ProxySettings proxyVia() {
    if (optionSet.has(PROXY_VIA)) {
      String proxyVia = (String) optionSet.valueOf(PROXY_VIA);
      return ProxySettings.fromString(proxyVia);
    }
    return NO_PROXY;
  }

  @Override
  public Stores getStores() {
    return stores;
  }

  @Override
  public FileSource filesRoot() {
    return fileSource;
  }

  @Override
  public MappingsLoader mappingsLoader() {
    return mappingsSource;
  }

  @Override
  public MappingsSaver mappingsSaver() {
    return mappingsSource;
  }

  @Override
  public Notifier notifier() {
    return new ConsoleNotifier(verboseLoggingEnabled());
  }

  @Override
  public boolean requestJournalDisabled() {
    return optionSet.has(DISABLE_REQUEST_JOURNAL);
  }

  public boolean bannerDisabled() {
    return optionSet.has(DISABLE_BANNER);
  }

  private boolean specifiesMaxRequestJournalEntries() {
    return optionSet.has(MAX_ENTRIES_REQUEST_JOURNAL);
  }

  @Override
  public Optional<Integer> maxRequestJournalEntries() {
    if (specifiesMaxRequestJournalEntries()) {
      return Optional.of(Integer.parseInt((String) optionSet.valueOf(MAX_ENTRIES_REQUEST_JOURNAL)));
    }
    return Optional.empty();
  }

  @Override
  public int containerThreads() {
    if (optionSet.has(CONTAINER_THREADS)) {
      return Integer.parseInt((String) optionSet.valueOf(CONTAINER_THREADS));
    }

    return DEFAULT_CONTAINER_THREADS;
  }

  @Override
  public String toString() {
    Map<String, Object> map = new LinkedHashMap<>();

    if (actualHttpPort != null) {
      map.put(PORT, actualHttpPort);
    }

    if (actualHttpsPort != null) {
      map.put(HTTPS_PORT, actualHttpsPort);
    }

    if (httpsSettings().enabled()) {
      map.put(HTTPS_KEYSTORE, nullToString(httpsSettings().keyStorePath()));
    }

    if (proxyVia() != NO_PROXY) {
      map.put(PROXY_VIA, proxyVia());
    }
    if (proxyUrl() != null) {
      map.put(PROXY_ALL, nullToString(proxyUrl()));
      map.put(PRESERVE_HOST_HEADER, shouldPreserveHostHeader());
    }

    BrowserProxySettings browserProxySettings = browserProxySettings();

    map.put(ENABLE_BROWSER_PROXYING, browserProxySettings.enabled());
    if (browserProxySettings.enabled()) {
      KeyStoreSettings keyStoreSettings = browserProxySettings.caKeyStore();
      map.put(TRUST_ALL_PROXY_TARGETS, browserProxySettings.trustAllProxyTargets());
      List<String> trustedProxyTargets = browserProxySettings.trustedProxyTargets();
      if (!trustedProxyTargets.isEmpty()) {
        map.put(TRUST_PROXY_TARGET, String.join(", ", trustedProxyTargets));
      }
      map.put(HTTPS_CA_KEYSTORE, keyStoreSettings.path());
      map.put(HTTPS_CA_KEYSTORE_TYPE, keyStoreSettings.type());
    }

    map.put(DISABLE_BANNER, bannerDisabled());

    if (recordMappingsEnabled()) {
      map.put(RECORD_MAPPINGS, recordMappingsEnabled());
      map.put(MATCH_HEADERS, matchingHeaders());
    }

    map.put(DISABLE_REQUEST_JOURNAL, requestJournalDisabled());
    map.put(VERBOSE, verboseLoggingEnabled());

    if (jettySettings().getAcceptQueueSize().isPresent()) {
      map.put(JETTY_ACCEPT_QUEUE_SIZE, jettySettings().getAcceptQueueSize().get());
    }

    if (jettySettings().getAcceptors().isPresent()) {
      map.put(JETTY_ACCEPTOR_THREAD_COUNT, jettySettings().getAcceptors().get());
    }

    if (jettySettings().getRequestHeaderSize().isPresent()) {
      map.put(JETTY_HEADER_BUFFER_SIZE, jettySettings().getRequestHeaderSize().get());
    }

    if (!(getAdminAuthenticator() instanceof NoAuthenticator)) {
      map.put(ADMIN_API_BASIC_AUTH, "enabled");
    }

    if (getHttpsRequiredForAdminApi()) {
      map.put(ADMIN_API_REQUIRE_HTTPS, "true");
    }

    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, Object> param : map.entrySet()) {
      int paddingLength = 29 - param.getKey().length();
      sb.append(param.getKey())
          .append(":")
          .append(" ".repeat(Math.max(0, paddingLength)))
          .append(nullToString(param.getValue()))
          .append("\n");
    }

    return sb.toString();
  }

  private String nullToString(Object value) {
    if (value == null) {
      return "(null)";
    }

    return value.toString();
  }

  @Override
  public AsynchronousResponseSettings getAsynchronousResponseSettings() {
    return new AsynchronousResponseSettings(
        isAsynchronousResponseEnabled(), getAsynchronousResponseThreads());
  }

  @Override
  public ChunkedEncodingPolicy getChunkedEncodingPolicy() {
    return optionSet.has(USE_CHUNKED_ENCODING)
        ? ChunkedEncodingPolicy.valueOf(
            optionSet.valueOf(USE_CHUNKED_ENCODING).toString().toUpperCase())
        : ChunkedEncodingPolicy.ALWAYS;
  }

  @Override
  public boolean getGzipDisabled() {
    return optionSet.has(DISABLE_GZIP);
  }

  @Override
  public boolean getStubRequestLoggingDisabled() {
    return optionSet.has(DISABLE_REQUEST_LOGGING);
  }

  @Override
  public boolean getStubCorsEnabled() {
    return optionSet.has(ENABLE_STUB_CORS);
  }

  @Override
  public long timeout() {
    return optionSet.has(TIMEOUT)
        ? Long.parseLong((String) optionSet.valueOf(TIMEOUT))
        : DEFAULT_TIMEOUT;
  }

  @Override
  public boolean getDisableOptimizeXmlFactoriesLoading() {
    return optionSet.has(DISABLE_OPTIMIZE_XML_FACTORIES_LOADING);
  }

  @Override
  public boolean getDisableStrictHttpHeaders() {
    return optionSet.has(DISABLE_STRICT_HTTP_HEADERS);
  }

  @Override
  public DataTruncationSettings getDataTruncationSettings() {
    return optionSet.has(LOGGED_RESPONSE_BODY_SIZE_LIMIT)
        ? new DataTruncationSettings(
            new Limit(
                Integer.parseInt((String) optionSet.valueOf(LOGGED_RESPONSE_BODY_SIZE_LIMIT))))
        : DataTruncationSettings.DEFAULTS;
  }

  @Override
  public NetworkAddressRules getProxyTargetRules() {
    NetworkAddressRules.Builder builder = NetworkAddressRules.builder();
    if (optionSet.has(ALLOW_PROXY_TARGETS)) {
      Arrays.stream(((String) optionSet.valueOf(ALLOW_PROXY_TARGETS)).split(","))
          .forEach(builder::allow);
    }

    if (optionSet.has(DENY_PROXY_TARGETS)) {
      Arrays.stream(((String) optionSet.valueOf(DENY_PROXY_TARGETS)).split(","))
          .forEach(builder::deny);
    }

    return builder.build();
  }

  @SuppressWarnings("unchecked")
  @Override
  public BrowserProxySettings browserProxySettings() {
    KeyStoreSettings keyStoreSettings =
        new KeyStoreSettings(
            KeyStoreSourceFactory.getAppropriateForJreVersion(
                (String) optionSet.valueOf(HTTPS_CA_KEYSTORE),
                (String) optionSet.valueOf(HTTPS_CA_KEYSTORE_TYPE),
                ((String) optionSet.valueOf(HTTPS_CA_KEYSTORE_PASSWORD)).toCharArray()));

    return new BrowserProxySettings.Builder()
        .enabled(optionSet.has(ENABLE_BROWSER_PROXYING))
        .trustAllProxyTargets(optionSet.has(TRUST_ALL_PROXY_TARGETS))
        .trustedProxyTargets((List<String>) optionSet.valuesOf(TRUST_PROXY_TARGET))
        .caKeyStoreSettings(keyStoreSettings)
        .build();
  }

  @Override
  public int proxyTimeout() {
    return optionSet.has(PROXY_TIMEOUT)
        ? Integer.valueOf((String) optionSet.valueOf(PROXY_TIMEOUT))
        : DEFAULT_TIMEOUT;
  }

  @Override
  public boolean getResponseTemplatingEnabled() {
    return !optionSet.has(DISABLE_RESPONSE_TEMPLATING);
  }

  @Override
  public boolean getResponseTemplatingGlobal() {
    return optionSet.has(GLOBAL_RESPONSE_TEMPLATING);
  }

  @Override
  public Long getMaxTemplateCacheEntries() {
    return optionSet.has(MAX_TEMPLATE_CACHE_ENTRIES)
        ? Long.valueOf(optionSet.valueOf(MAX_TEMPLATE_CACHE_ENTRIES).toString())
        : null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<String> getTemplatePermittedSystemKeys() {
    return optionSet.has(PERMITTED_SYSTEM_KEYS)
        ? Set.copyOf((List<String>) optionSet.valuesOf(PERMITTED_SYSTEM_KEYS))
        : Collections.emptySet();
  }

  @Override
  public boolean getTemplateEscapingDisabled() {
    return true;
  }

  private boolean isAsynchronousResponseEnabled() {
    return optionSet.has(ASYNCHRONOUS_RESPONSE_ENABLED)
        ? Boolean.valueOf((String) optionSet.valueOf(ASYNCHRONOUS_RESPONSE_ENABLED))
        : false;
  }

  private int getAsynchronousResponseThreads() {
    return Integer.valueOf((String) optionSet.valueOf(ASYNCHRONOUS_RESPONSE_THREADS));
  }
}
