package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.google.common.base.Objects;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matches requests containing form parameters in its body.
 * As a precondition for any match, the request header <code>Content-Type=application/x-www-form-urlencoded</code> must be present.
 * <br>
 * Note: <code>Content-Type=multipart/form-data</code> is not yet supported.
 */
public class BodyRequestPattern extends RequestPattern {
    private List<FormFieldPattern> formFieldPatterns;

    public BodyRequestPattern(UrlPattern url,
                              RequestMethod method,
                              Map<String, MultiValuePattern> headers,
                              Map<String, MultiValuePattern> queryParams,
                              Map<String, StringValuePattern> cookies,
                              BasicCredentials basicAuthCredentials,
                              List<StringValuePattern> bodyPatterns,
                              List<FormFieldPattern> formFieldPatterns,
                              CustomMatcherDefinition customMatcherDefinition) {
        super(url, method, headers, queryParams, cookies, basicAuthCredentials, bodyPatterns, customMatcherDefinition);
        this.formFieldPatterns = formFieldPatterns;
    }

    public BodyRequestPattern(@JsonProperty("url") String url,
                              @JsonProperty("urlPattern") String urlPattern,
                              @JsonProperty("urlPath") String urlPath,
                              @JsonProperty("urlPathPattern") String urlPathPattern,
                              @JsonProperty("method") RequestMethod method,
                              @JsonProperty("headers") Map<String, MultiValuePattern> headers,
                              @JsonProperty("queryParameters") Map<String, MultiValuePattern> queryParams,
                              @JsonProperty("cookies") Map<String, StringValuePattern> cookies,
                              @JsonProperty("basicAuth") BasicCredentials basicAuthCredentials,
                              @JsonProperty("bodyPatterns") List<StringValuePattern> bodyPatterns,
                              @JsonProperty("formFieldPatterns") List<FormFieldPattern> formFieldPatterns,
                              @JsonProperty("customMatcher") CustomMatcherDefinition customMatcherDefinition) {
        super(url, urlPattern, urlPath, urlPathPattern, method, headers, queryParams, cookies, basicAuthCredentials, bodyPatterns, customMatcherDefinition);
        this.formFieldPatterns = formFieldPatterns;
    }

    public BodyRequestPattern(RequestMatcher customMatcher) {
        super(customMatcher);
    }

    public BodyRequestPattern(CustomMatcherDefinition customMatcherDefinition) {
        super(customMatcherDefinition);
    }

    public List<FormFieldPattern> getFormFieldPatterns() {
        return formFieldPatterns;
    }

    @Override
    public MatchResult match(Request request) {
        MatchResult matchResult = super.match(request);
        if (matchResult.isExactMatch())
            matchResult = match(request, this.formFieldPatterns);
        return matchResult;
    }

    public MatchResult match(Request request, Map<String, RequestMatcherExtension> customMatchers) {
        MatchResult matchResult = super.match(request, customMatchers);
        if (matchResult.isExactMatch())
            matchResult = match(request, this.formFieldPatterns);
        return matchResult;
    }

    private static MatchResult match(Request request, List<FormFieldPattern> formFieldPatterns) {
        if (formFieldPatterns != null) {
            if (Objects.equal(request.getHeader("Content-Type"), "application/x-www-form-urlencoded")) {
                Map<String, String> formFields = parseFormFields(request.getBodyAsString());

                for (FormFieldPattern formFieldPattern : formFieldPatterns) {
                    boolean match = false;
                    if (formFieldPattern.getNamePattern() instanceof EqualToPattern) {
                        //optimize for simple equality check
                        String value = formFields.get(formFieldPattern.getNamePattern().getValue());
                        if (formFieldPattern.getValuePattern().match(value).isExactMatch())
                            match = true;
                    } else {
                        for (Map.Entry<String, String> formField : formFields.entrySet()) {
                            if (formFieldPattern.getNamePattern().match(formField.getKey()).isExactMatch()
                                    && formFieldPattern.getValuePattern().match(formField.getValue()).isExactMatch()) {
                                match = true;
                                break;
                            }
                        }
                    }
                    if (!match)
                        return MatchResult.of(false);
                }
                return MatchResult.of(true);
            }
        } else {
            return MatchResult.of(true);
        }
        return MatchResult.of(false);
    }

    private static Pattern KEY_VALUE_PATTERN = Pattern.compile("([^=&]*)=([^&]*)(?:|&)");

    static LinkedHashMap<String, String> parseFormFields(String input) {
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        Matcher matcher = KEY_VALUE_PATTERN.matcher(input);
        while (matcher.find())
            try {
                params.put(URLDecoder.decode(matcher.group(1), "UTF-8"), URLDecoder.decode(matcher.group(2), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }
        return params;
    }

    static boolean containsPair(LinkedHashMap<String, String> formParams, String key, String value) {
        if (key == null)
            throw new NullPointerException();

        String actualValue = formParams.get(key);
        return Objects.equal(value, actualValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BodyRequestPattern that = (BodyRequestPattern) o;

        return formFieldPatterns != null ? formFieldPatterns.equals(that.formFieldPatterns) : that.formFieldPatterns == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (formFieldPatterns != null ? formFieldPatterns.hashCode() : 0);
        return result;
    }
}
