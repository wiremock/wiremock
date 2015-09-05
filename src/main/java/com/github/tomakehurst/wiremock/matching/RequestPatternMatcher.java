package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.Request;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.http.RequestMethod.ANY;
import static com.github.tomakehurst.wiremock.matching.ValuePattern.matching;
import static com.google.common.collect.Iterables.*;

public class RequestPatternMatcher {
    private final MatcherObserver matcherObserver;

    public RequestPatternMatcher(MatcherObserver matcherObserver) {
        this.matcherObserver = matcherObserver;
    }

    public boolean matches(Request request, RequestPattern requestPattern) {
        boolean urlMatches = urlIsMatch(request, requestPattern);
        boolean methodMatches = methodMatches(request, requestPattern);
        boolean requiredAbsentHeadersAreNotPresent = requiredAbsentHeadersAreNotPresentIn(request, requestPattern);
        boolean headersMatch = headersMatch(request, requestPattern);
        boolean queryParametersMatch = queryParametersMatch(request, requestPattern);
        boolean bodyMatches = bodyMatches(request, requestPattern);
        boolean requestMatchesPattern = urlMatches &&
                methodMatches &&
                requiredAbsentHeadersAreNotPresent &&
                headersMatch &&
                queryParametersMatch &&
                bodyMatches;
        matcherObserver.onMatchingResult(requestMatchesPattern, urlMatches, methodMatches, requiredAbsentHeadersAreNotPresent, headersMatch, queryParametersMatch, bodyMatches, request, requestPattern);
        return requestMatchesPattern;
    }

    private boolean urlIsMatch(Request request, RequestPattern requestPattern) {
        String candidateUrl = request.getUrl();
        boolean matched;
        if (requestPattern.getUrl() != null) {
            matched = requestPattern.getUrl().equals(candidateUrl);
        } else if (requestPattern.getUrlPattern() != null) {
            matched = candidateUrl.matches(requestPattern.getUrlPattern());
        } else if (requestPattern.getUrlPathPattern() != null) {
            matched = candidateUrl.matches(requestPattern.getUrlPathPattern().concat(".*"));
        } else {
            matched = candidateUrl.startsWith(requestPattern.getUrlPath());
        }

        return matched;
    }

    private boolean methodMatches(Request request, RequestPattern requestPattern) {
        boolean matched = requestPattern.getMethod().equals(ANY) || request.getMethod().equals(requestPattern.getMethod());
        if (!matched) {
            notifier().info(String.format("URL %s is match, but method %s is not", request.getUrl(), request.getMethod()));
        }

        return matched;
    }

    private boolean requiredAbsentHeadersAreNotPresentIn(final Request request, RequestPattern requestPattern) {
        return !any(requiredAbsentHeaderKeys(requestPattern), new Predicate<String>() {
            public boolean apply(String key) {
                return request.getAllHeaderKeys().contains(key);
            }
        });
    }


    private Set<String> requiredAbsentHeaderKeys(RequestPattern requestPattern) {
        Map<String, ValuePattern> headerPatterns = requestPattern.getHeaders();
        if (headerPatterns == null) {
            return ImmutableSet.of();
        }

        return ImmutableSet.copyOf(filter(transform(headerPatterns.entrySet(), TO_KEYS_WHERE_VALUE_ABSENT), REMOVING_NULL));
    }

    private boolean headersMatch(final Request request, RequestPattern requestPattern) {
        return noHeadersAreRequiredToBePresent(requestPattern) ||
                all(requestPattern.getHeaders().entrySet(), matchHeadersIn(request));
    }

    private boolean queryParametersMatch(Request request, RequestPattern requestPattern) {
        return (requestPattern.getQueryParameters() == null ||
                all(requestPattern.getQueryParameters().entrySet(), matchQueryParametersIn(request)));
    }

    private boolean bodyMatches(Request request, RequestPattern requestPattern) {
        List<ValuePattern> bodyPatterns = requestPattern.getBodyPatterns();
        if (bodyPatterns == null) {
            return true;
        }

        boolean matches = all(bodyPatterns, matching(request.getBodyAsString()));

        if (!matches) {
            notifier().info(String.format("URL %s is match, but body is not: %s", request.getUrl(), request.getBodyAsString()));
        }

        return matches;
    }

    private boolean noHeadersAreRequiredToBePresent(RequestPattern requestPattern) {
        return requestPattern.getHeaders() == null || allHeaderPatternsSpecifyAbsent(requestPattern);
    }

    private static Predicate<Map.Entry<String, ValuePattern>> matchHeadersIn(final Request request) {
        return new Predicate<Map.Entry<String, ValuePattern>>() {
            public boolean apply(Map.Entry<String, ValuePattern> headerPattern) {
                ValuePattern headerValuePattern = headerPattern.getValue();
                String key = headerPattern.getKey();
                HttpHeader header = request.header(key);

                boolean match = header.hasValueMatching(headerValuePattern);

                if (!match) {
                    notifier().info(String.format(
                            "URL %s is match, but header %s is not. For a match, value should %s",
                            request.getUrl(),
                            key,
                            headerValuePattern.toString()));
                }

                return match;
            }
        };
    }

    private Predicate<? super Map.Entry<String, ValuePattern>> matchQueryParametersIn(final Request request) {
        return new Predicate<Map.Entry<String, ValuePattern>>() {
            public boolean apply(Map.Entry<String, ValuePattern> entry) {
                ValuePattern valuePattern = entry.getValue();
                String key = entry.getKey();
                Optional<QueryParameter> queryParam = Optional.fromNullable(request.queryParameter(key));
                boolean match = queryParam.isPresent() && queryParam.get().hasValueMatching(valuePattern);

                if (!match) {
                    notifier().info(String.format(
                            "URL %s is match, but query parameter %s is not. For a match, value should %s",
                            request.getUrl(),
                            key,
                            valuePattern.toString()));
                }

                return match;
            }
        };
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

    private boolean allHeaderPatternsSpecifyAbsent(RequestPattern requestPattern) {
        return size(filter(requestPattern.getHeaders().values(), new Predicate<ValuePattern>() {
            public boolean apply(ValuePattern headerPattern) {
                return !headerPattern.nullSafeIsAbsent();
            }
        })) == 0;
    }
}
