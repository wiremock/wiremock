package com.github.tomakehurst.wiremock.client;

/**
 * Matches the number of requests made using relational predicates.
 */
public class CountMatchingStrategy {

    public static final CountMatchingMode LESS_THAN =
            new CountMatchingMode() {
                @Override
                public String getFriendlyName() {
                    return "Less than";
                }

                @Override
                public boolean test(Integer actual, Integer expected) {
                    return actual < expected;
                }
            };

    public static final CountMatchingMode LESS_THAN_OR_EQUAL =
            new CountMatchingMode() {
                @Override
                public String getFriendlyName() {
                    return "Less than or exactly";
                }

                @Override
                public boolean test(Integer actual, Integer expected) {
                    return actual <= expected;
                }
            };

    public static final CountMatchingMode EQUAL_TO =
            new CountMatchingMode() {
                @Override
                public String getFriendlyName() {
                    return "Exactly";
                }

                @Override
                public boolean test(Integer actual, Integer expected) {
                    return actual.equals(expected);
                }
            };

    public static final CountMatchingMode GREATER_THAN_OR_EQUAL =
            new CountMatchingMode() {
                @Override
                public String getFriendlyName() {
                    return "More than or exactly";
                }

                @Override
                public boolean test(Integer actual, Integer expected) {
                    return actual >= expected;
                }
            };

    public static final CountMatchingMode GREATER_THAN =
            new CountMatchingMode() {
                @Override
                public String getFriendlyName() {
                    return "More than";
                }

                @Override
                public boolean test(Integer actual, Integer expected) {
                    return actual > expected;
                }
            };

    private CountMatchingMode mode;
    private int expected;

    public CountMatchingStrategy(CountMatchingMode mode, int expected) {
        this.mode = mode;
        this.expected = expected;
    }

    public boolean match(int actual) {
        return mode.test(actual, expected);
    }

    @Override
    public String toString() {
        return String.format("%s %d", mode.getFriendlyName(), expected);
    }

}
