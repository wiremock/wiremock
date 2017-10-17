package com.github.tomakehurst.wiremock.verification.diff;

import com.github.tomakehurst.wiremock.matching.NamedValueMatcher;
import org.apache.commons.lang3.StringUtils;

class DiffLine<V> {

    private final String requestAttribute;
    private final NamedValueMatcher<V> pattern;
    private final V value;
    private final String printedPatternValue;

    public DiffLine(String requestAttribute, NamedValueMatcher<V> pattern, V value, String printedPatternValue) {
        this.requestAttribute = requestAttribute;
        this.pattern = pattern;
        this.value = value;
        this.printedPatternValue = printedPatternValue;
    }

    public String getRequestAttribute() {
        return requestAttribute;
    }

    public Object getActual() {
        return value;
    }

    public String getPrintedPatternValue() {
        return printedPatternValue;
    }

    public boolean isForNonMatch() {
        return !isExactMatch();
    }

    protected boolean isExactMatch() {
        return pattern.match(value).isExactMatch();
    }

    public String getMessage() {
        if (value == null || StringUtils.isEmpty(value.toString())) {
            return requestAttribute + " is not present";
        }

        return isExactMatch() ?
            null :
            requestAttribute + " does not match";
    }
}
