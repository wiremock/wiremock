/*
 * Copyright (C) 2021-2025 Thomas Akehurst
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

import static java.lang.Math.min;
import static java.lang.Math.round;

public abstract class AbstractNumberMatchResult extends MatchResult {

    protected static final float EXACT_MATCH = 0.0f;
    protected static final float NO_MATCH = 1.0f;
    private static final double MAX_LOG_DIFF = 176.0;
    private final Double expectedValue;
    private final String value;


    public AbstractNumberMatchResult(Number expectedValue, String value) {
        this.expectedValue = expectedValue.doubleValue();
        this.value = value;
    }

    private static double getShiftValue(double expectedDouble, double actualDouble) {
        if (expectedDouble < 0.0 || actualDouble < 0.0) {
            return min(expectedDouble, actualDouble);
        } else {
            return EXACT_MATCH;
        }
    }

    private static boolean areValuesTooBig(double expectedDouble, double actualDouble) {
        return expectedDouble == Double.POSITIVE_INFINITY ||
            actualDouble == Double.POSITIVE_INFINITY ||
            (expectedDouble == Double.MAX_VALUE && actualDouble == Double.MAX_VALUE);
    }

    private static double calculateDistance(double expectedDouble, double actualDouble) {
        double logA = Math.log(expectedDouble);
        double logB = Math.log(actualDouble);
        double logDiff = Math.abs(logA - logB);
        double normalized = logDiff / MAX_LOG_DIFF;

        return round(min(normalized, 1.0) * 100) / 100.0;
    }

    abstract protected boolean isExactMatch(double expected, double actual);

    @Override
    public boolean isExactMatch() {
        try {
            var actualDouble = Double.parseDouble(value);

            return isExactMatch(expectedValue, actualDouble);
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }

    @Override
    public double getDistance() {
        try {
            var parsedActual = Double.parseDouble(value);
            if (isExactMatch(expectedValue, parsedActual)) {
                return EXACT_MATCH;
            }
            var shiftValue = getShiftValue(expectedValue, parsedActual);
            var expectedDouble = expectedValue + shiftValue;
            var actualDouble = parsedActual + shiftValue;

            if (areValuesTooBig(expectedDouble, actualDouble)) {
                return NO_MATCH;
            }
            double actualDistance = calculateDistance(expectedDouble, actualDouble);
            if (actualDistance == EXACT_MATCH) {
                return 0.01;
            } else {
                return actualDistance;
            }
        } catch (NumberFormatException | NullPointerException e) {
            return 1.0;
        }
    }
}
