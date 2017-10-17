package com.github.tomakehurst.wiremock.verification.diff;

import com.google.common.base.Function;
import com.google.common.base.Joiner;

import java.util.List;

import static com.google.common.collect.FluentIterable.from;

public class JUnitStyleDiffRenderer {

    public String render(Diff diff) {
        if (diff.hasCustomMatcher()) {
            return "(Request pattern had a custom matcher so no diff can be shown)";
        }

        List<DiffLine<?>> lines = diff.getLines();

        String expected = Joiner.on("\n")
            .join(from(lines).transform(EXPECTED));
        String actual = Joiner.on("\n")
            .join(from(lines).transform(ACTUAL));

        return lines.isEmpty() ? "" : junitStyleDiffMessage(expected, actual);
    }

    public static String junitStyleDiffMessage(Object expected, Object actual) {
        return String.format(" expected:<\n%s> but was:<\n%s>", expected, actual);
    }

    private static Function<DiffLine<?>, Object> EXPECTED = new Function<DiffLine<?>, Object>() {
        @Override
        public Object apply(DiffLine<?> line) {
            return line.isForNonMatch() ?
                line.getPrintedPatternValue() :
                line.getActual();
        }
    };

    private static Function<DiffLine<?>, Object> ACTUAL = new Function<DiffLine<?>, Object>() {
        @Override
        public Object apply(DiffLine<?> input) {
            return input.getActual();
        }
    };
}
