/*
 * Copyright (C) 2011 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;
import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.client.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.*;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import java.net.URI;
import java.util.*;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.http.RequestMethod.ANY;
import static com.github.tomakehurst.wiremock.matching.ValuePattern.TO_STRING_VALUE_PATTERN;
import static com.github.tomakehurst.wiremock.matching.ValuePattern.matching;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static java.util.Arrays.asList;

@JsonSerialize(include=Inclusion.NON_NULL)
public class RequestPattern {

    private String urlPattern;
	private String url;
    private String urlPath;
	private String urlPathPattern;
    private RequestMethod method;
    private Map<String, ValuePattern> headerPatterns;
    private Map<String, ValuePattern> cookiePatterns;
    private Map<String, ValuePattern> queryParamPatterns;
	private BasicCredentials basicAuthCredentials;
    private List<ValuePattern> bodyPatterns;

	private final RequestMatcher defaultMatcher = new RequestMatcher() {

        @Override
        public MatchResult match(Request request) {
            return MatchResult.of(RequestPattern.this.allElementsMatch(request));
        }

        @Override
        public String getName() {
            return "default";
        }

	};
	private RequestMatcher matcher = defaultMatcher;

	private CustomMatcherDefinition customMatcherDefinition;

	public RequestPattern(RequestMatcher customMatcher) {
		this.matcher = customMatcher;
	}

	public RequestPattern(String customMatcherName, Parameters matcherParameters) {
		customMatcherDefinition = new CustomMatcherDefinition(customMatcherName, matcherParameters);
	}

	public RequestPattern(RequestMethod method, String url, Map<String, ValuePattern> headerPatterns, Map<String, ValuePattern> queryParamPatterns) {
        this.url = url;
        this.method = method;
        this.headerPatterns = headerPatterns;
        this.queryParamPatterns = queryParamPatterns;
    }

    public RequestPattern(RequestMethod method, String url, Map<String, ValuePattern> headerPatterns) {
		this.url = url;
		this.method = method;
		this.headerPatterns = headerPatterns;
	}

	public RequestPattern(RequestMethod method) {
		this.method = method;
	}

	public RequestPattern(RequestMethod method, String url) {
		this.url = url;
		this.method = method;
	}

	public RequestPattern() {
	}

    public static RequestPattern everything() {
        RequestPattern requestPattern = new RequestPattern(RequestMethod.ANY);
        requestPattern.setUrlPattern(".*");
        return requestPattern;
    }

    public static RequestPattern buildRequestPatternFrom(String json) {
        return Json.read(json, RequestPattern.class);
    }

    private void assertIsInValidState() {
        if (from(asList(url, urlPath, urlPattern, urlPathPattern)).filter(notNull()).size() > 1) {
			throw new IllegalStateException("Only one of url, urlPattern, urlPath or urlPathPattern may be set");
		}
	}

	public boolean isMatchedBy(Request request, Map<String, RequestMatcherExtension> customMatchers) {
		if (customMatcherDefinition != null) {
			RequestMatcherExtension requestMatcher = customMatchers.get(customMatcherDefinition.getName());
			return requestMatcher.match(request).isExactMatch();
		}

		return matcher.match(request).isExactMatch();
	}

	public boolean isMatchedBy(Request request) {
		return isMatchedBy(request, Collections.<String, RequestMatcherExtension>emptyMap());
	}

	private boolean allElementsMatch(Request request) {
		return (urlIsMatch(request) &&
				methodMatches(request) &&
                requiredAbsentHeadersAreNotPresentIn(request) &&
				headersMatch(request) &&
				cookiesMatch(request) &&
                queryParametersMatch(request) &&
				bodyMatches(request));
	}

    private boolean urlIsMatch(Request request) {
		String candidateUrl = request.getUrl();
		boolean matched;
		if (url != null) {
            matched = url.equals(candidateUrl);
        } else if (urlPattern != null) {
			matched = candidateUrl.matches(urlPattern);
		} else if (urlPathPattern != null) {
			matched = candidateUrl.matches(urlPathPattern.concat(".*"));
		} else {
            matched = URI.create(candidateUrl).getPath().equals(urlPath);
        }

		return matched;
	}

	private boolean methodMatches(Request request) {
		boolean matched = method.equals(ANY) || request.getMethod().equals(method);
		if (!matched) {
			notifier().info(String.format("URL %s is match, but method %s is not", request.getUrl(), request.getMethod()));
		}

		return matched;
	}

    private boolean requiredAbsentHeadersAreNotPresentIn(final Request request) {
        return !any(requiredAbsentHeaderKeys(), new Predicate<String>() {
            public boolean apply(String key) {
                return request.getAllHeaderKeys().contains(key);
            }
        });
    }

    private Set<String> requiredAbsentHeaderKeys() {
        if (headerPatterns == null) {
            return ImmutableSet.of();
        }

        return ImmutableSet.copyOf(filter(transform(headerPatterns.entrySet(), TO_KEYS_WHERE_VALUE_ABSENT), REMOVING_NULL));
    }

	private boolean headersMatch(final Request request) {
        Map<String, ValuePattern> combinedHeaders = basicAuthCredentials != null ?
            combineBasicAuthAndOtherHeaders() :
            headerPatterns;

        return noHeadersAreRequiredToBePresent(combinedHeaders) ||
               all(combinedHeaders.entrySet(), matchHeadersIn(request));
	}

    private Map<String, ValuePattern> combineBasicAuthAndOtherHeaders() {
        Map<String, ValuePattern> combinedHeaders = headerPatterns;
        ImmutableMap.Builder<String, ValuePattern> allHeadersBuilder =
            ImmutableMap.<String, ValuePattern>builder()
            .putAll(Optional.fromNullable(combinedHeaders).or(Collections.<String, ValuePattern>emptyMap()));
        allHeadersBuilder.put(AUTHORIZATION, basicAuthCredentials.asAuthorizationHeaderValue());
        combinedHeaders = allHeadersBuilder.build();
        return combinedHeaders;
    }

    private boolean queryParametersMatch(Request request) {
        return (queryParamPatterns == null ||
                all(queryParamPatterns.entrySet(), matchQueryParametersIn(request)));
    }

    private static boolean noHeadersAreRequiredToBePresent(Map<String, ValuePattern> headerPatterns) {
        return headerPatterns == null || allHeaderPatternsSpecifyAbsent(headerPatterns);
    }

    private static boolean allHeaderPatternsSpecifyAbsent(Map<String, ValuePattern> headerPatterns) {
        return size(filter(headerPatterns.values(), new Predicate<ValuePattern>() {
            public boolean apply(ValuePattern headerPattern) {
                return !headerPattern.nullSafeIsAbsent();
            }
        })) == 0;
    }

    private boolean cookiesMatch(final Request request) {
        return (cookiePatterns == null ||
                all(cookiePatterns.entrySet(), matchCookiesIn(request)));
    }

    private boolean bodyMatches(Request request) {
		if (bodyPatterns == null) {
			return true;
		}

		boolean matches = all(bodyPatterns, matching(request.getBodyAsString()));

		if (!matches) {
			notifier().info(String.format("URL %s is match, but body is not: %s", request.getUrl(), request.getBodyAsString()));
		}

		return matches;
	}

	public String getUrlPattern() {
		return urlPattern;
	}

	public void setUrlPattern(String urlPattern) {
		this.urlPattern = urlPattern;
		assertIsInValidState();
	}

	public RequestMethod getMethod() {
		return method;
	}

	public void setMethod(RequestMethod method) {
		this.method = method;
	}

	public Map<String, ValuePattern> getHeaders() {
		return headerPatterns;
	}

    public Map<String, ValuePattern> getQueryParameters() {
        return queryParamPatterns;
    }

	public CustomMatcherDefinition getCustomMatcher() {
		return customMatcherDefinition;
	}

	public void setCustomMatcher(CustomMatcherDefinition customMatcherDefinition) {
		this.customMatcherDefinition = customMatcherDefinition;
	}

    public void setQueryParameters(Map<String, ValuePattern> queryParamPatterns) {
        this.queryParamPatterns = queryParamPatterns;
    }

    public void addHeader(String key, ValuePattern pattern) {
		if (headerPatterns == null) {
			headerPatterns = newLinkedHashMap();
		}

		headerPatterns.put(key, pattern);
	}

    public void addQueryParam(String key, ValuePattern valuePattern) {
        if (queryParamPatterns == null) {
            queryParamPatterns = newLinkedHashMap();
        }

        queryParamPatterns.put(key, valuePattern);
    }

	public void setHeaders(Map<String, ValuePattern> headers) {
		this.headerPatterns = headers;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
		assertIsInValidState();
	}

    public String getUrlPath() {
        return urlPath;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
        assertIsInValidState();
    }

	public String getUrlPathPattern() {
		return urlPathPattern;
	}

	public void setUrlPathPattern(String urlPathPattern) {
		this.urlPathPattern = urlPathPattern;
		assertIsInValidState();
	}

	public List<ValuePattern> getBodyPatterns() {
		return bodyPatterns;
	}

	public void setBodyPatterns(List<ValuePattern> bodyPatterns) {
		this.bodyPatterns = bodyPatterns;
	}

    public BasicCredentials getBasicAuth() {
        return basicAuthCredentials;
    }

    public void setBasicAuth(BasicCredentials basicCredentials) {
        this.basicAuthCredentials = basicCredentials;
    }

    public Map<String, ValuePattern> getCookies() {
        return cookiePatterns;
    }

    public void setCookies(Map<String, ValuePattern> cookies) {
		this.cookiePatterns = cookies;
	}

	@JsonIgnore
	public boolean hasCustomMatcher() {
		return matcher != defaultMatcher;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RequestPattern that = (RequestPattern) o;
		return Objects.equals(urlPattern, that.urlPattern) &&
				Objects.equals(url, that.url) &&
				Objects.equals(urlPath, that.urlPath) &&
				Objects.equals(urlPathPattern, that.urlPathPattern) &&
				Objects.equals(method, that.method) &&
				Objects.equals(headerPatterns, that.headerPatterns) &&
				Objects.equals(queryParamPatterns, that.queryParamPatterns) &&
				Objects.equals(bodyPatterns, that.bodyPatterns);
	}

	@Override
	public int hashCode() {
		return Objects.hash(urlPattern, url, urlPath, urlPathPattern, method, headerPatterns, queryParamPatterns, bodyPatterns);
	}

	@Override
	public String toString() {
		return Json.write(this);
	}

    public static void main(String[] args) {
        System.out.println(new RequestPatternBuilder(RequestMethod.GET, WireMock.urlEqualTo("/test")).withBasicAuth(new BasicCredentials("user", "pass")).build().toString());
    }
    private static final Function<Map.Entry<String,ValuePattern>,String> TO_KEYS_WHERE_VALUE_ABSENT = new Function<Map.Entry<String, ValuePattern>, String>() {
        public String apply(Map.Entry<String, ValuePattern> input) {
            return input.getValue().nullSafeIsAbsent() ? input.getKey() : null;
        }
    };

    private static final Predicate<String> REMOVING_NULL = new Predicate<String>() {
        public boolean apply(String input) {
            return input != null;
        }
    };

    private Predicate<? super Map.Entry<String, ValuePattern>> matchHeadersIn(final Request request) {
        return matchIn("header", request, new PatternMatcher() {
            public boolean matches(Request request, ValuePattern valuePattern, String key) {
                HttpHeader header = request.header(key);
                return header.hasValueMatching(valuePattern);
            }
        });
    }

    private Predicate<? super Map.Entry<String, ValuePattern>> matchCookiesIn(final Request request) {
        return matchIn("cookie", request, new PatternMatcher() {
            public boolean matches(Request request, final ValuePattern valuePattern, String key) {
                Optional<Cookie> maybeCookie = Optional.fromNullable(request.getCookies().get(key));
                return maybeCookie.transform(new Function<Cookie, Boolean>() {
                    public Boolean apply(Cookie cookie) {
                        return valuePattern.isMatchFor(cookie.getValue());
                    }
                }).or(false);
            }
        });
    }

    private Predicate<? super Map.Entry<String, ValuePattern>> matchQueryParametersIn(final Request request) {
        return matchIn("query parameter", request, new PatternMatcher() {
            public boolean matches(Request request, ValuePattern valuePattern, String key) {
                Optional<QueryParameter> queryParam = Optional.fromNullable(request.queryParameter(key));
                return queryParam.isPresent() && queryParam.get().hasValueMatching(valuePattern);
            }
        });
    }

    private static Predicate<Map.Entry<String, ValuePattern>> matchIn(final String elementName, final Request request, final PatternMatcher patternMatcher) {
        return new Predicate<Map.Entry<String, ValuePattern>>() {
            public boolean apply(Map.Entry<String, ValuePattern> entry) {
                ValuePattern pattern = entry.getValue();
                String key = entry.getKey();

                boolean match = patternMatcher.matches(request, pattern, key);

                if (!match) {
                    notifier().info(String.format(
                        "URL %s is match, but %s %s is not. For a match, value should %s",
                        request.getUrl(),
                        elementName,
                        key,
                        pattern.toString()));
                }

                return match;
            }
        };
    }

    public NewRequestPattern toNewRequestPattern() {
        return customMatcherDefinition != null ?
            new NewRequestPattern(customMatcherDefinition) :
                hasCustomMatcher() ?
                new NewRequestPattern(matcher) :
                    new NewRequestPattern(
                    UrlPattern.fromOneOf(getUrl(), getUrlPattern(), getUrlPath(), getUrlPathPattern()),
                    getMethod(),
                    toMultiValuePatternMap(getHeaders()),
                    toMultiValuePatternMap(getQueryParameters()),
                    toStringValuePatternMap(getCookies()),
                    getBasicAuth(),
                    toBodyPatterns(getBodyPatterns())
                );
    }

    private Map<String, StringValuePattern> toStringValuePatternMap(Map<String, ValuePattern> valuePatternMap) {
        if (valuePatternMap == null || valuePatternMap.isEmpty()) {
            return null;
        }

        return Maps.transformValues(valuePatternMap, TO_STRING_VALUE_PATTERN);
    }

    private static List<StringValuePattern> toBodyPatterns(List<ValuePattern> bodyPatterns) {
        return (bodyPatterns != null) ?
            from(bodyPatterns).transform(TO_STRING_VALUE_PATTERN).toList() :
            null;
    }

    private static Map<String, MultiValuePattern> toMultiValuePatternMap(Map<String, ValuePattern> valuePatternMap) {
        if (valuePatternMap == null) {
            return null;
        }

        return Maps.transformValues(valuePatternMap, new Function<ValuePattern, MultiValuePattern>() {
            @Override
            public MultiValuePattern apply(ValuePattern input) {
                return new MultiValuePattern(input.toStringValuePattern());
            }
        });
    }

    static interface PatternMatcher {
        boolean matches(Request request, ValuePattern valuePattern, String key);

    }
}
