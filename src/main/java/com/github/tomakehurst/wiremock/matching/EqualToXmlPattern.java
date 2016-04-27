package com.github.tomakehurst.wiremock.matching;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static org.xmlunit.diff.ComparisonType.*;

public class EqualToXmlPattern extends StringValuePattern {

    private static List<ComparisonType> COUNTED_COMPARISONS = ImmutableList.of(
        SCHEMA_LOCATION,
        NO_NAMESPACE_SCHEMA_LOCATION,
        NODE_TYPE,
        NAMESPACE_URI,
        TEXT_VALUE,
        PROCESSING_INSTRUCTION_TARGET,
        PROCESSING_INSTRUCTION_DATA,
        ELEMENT_NUM_ATTRIBUTES,
        ATTR_VALUE,
        CHILD_NODELIST_LENGTH,
        CHILD_NODELIST_SEQUENCE,
        CHILD_LOOKUP,
        ATTR_NAME_LOOKUP
    );

    public EqualToXmlPattern(String expectedValue) {
        super(expectedValue);
    }

    public String getEqualToXml() {
        return expectedValue;
    }

    @Override
    public MatchResult match(final String value) {
        return new MatchResult() {
            @Override
            public boolean isExactMatch() {
                Diff diff = DiffBuilder.compare(Input.from(expectedValue))
                    .withTest(value)
                    .withComparisonController(ComparisonControllers.StopWhenDifferent)
                    .ignoreWhitespace()
                    .ignoreComments()
                    .withDifferenceEvaluator(IGNORE_UNCOUNTED_COMPARISONS)
                    .build();

                return !diff.hasDifferences();
            }

            @Override
            public double getDistance() {
                final AtomicInteger totalComparisons = new AtomicInteger(0);
                final AtomicInteger differences = new AtomicInteger(0);

                Diff diff = DiffBuilder.compare(Input.from(expectedValue))
                    .withTest(value)
                    .ignoreWhitespace()
                    .ignoreComments()
                    .withDifferenceEvaluator(IGNORE_UNCOUNTED_COMPARISONS)
                    .withComparisonListeners(new ComparisonListener() {
                        @Override
                        public void comparisonPerformed(Comparison comparison, ComparisonResult outcome) {
                            if (COUNTED_COMPARISONS.contains(comparison.getType()) && comparison.getControlDetails().getValue() != null) {
                                totalComparisons.incrementAndGet();
                                if (outcome == ComparisonResult.DIFFERENT) {
                                    differences.incrementAndGet();
                                }
                            }
                        }
                    })
                    .build();

                notifier().info(
                    Joiner.on("\n").join(diff.getDifferences())
                );

                return differences.doubleValue() / totalComparisons.doubleValue();
            }
        };
    }

    private static final DifferenceEvaluator IGNORE_UNCOUNTED_COMPARISONS = new DifferenceEvaluator() {
        @Override
        public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
            if (COUNTED_COMPARISONS.contains(comparison.getType()) && comparison.getControlDetails().getValue() != null) {
                return outcome;
            }

            return ComparisonResult.EQUAL;
        }
    };

}
