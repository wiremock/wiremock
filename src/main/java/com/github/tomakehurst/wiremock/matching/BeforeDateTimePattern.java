package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class BeforeDateTimePattern extends AbstractDateTimePattern {

    public BeforeDateTimePattern(String dateTimeSpec) {
        super(dateTimeSpec);
    }

    public BeforeDateTimePattern(
            @JsonProperty("before") String dateTimeSpec,
            @JsonProperty("actualFormat") String actualDateFormat,
            @JsonProperty("truncateExpected") String truncateExpected,
            @JsonProperty("truncateActual") String truncateActual
    ) {
        super(dateTimeSpec, actualDateFormat, truncateExpected, truncateActual);
    }

    @Override
    protected MatchResult getMatchResult(ZonedDateTime zonedExpected, LocalDateTime localExpected, ZonedDateTime zonedActual, LocalDateTime localActual) {

        return new AbstractDateTimeMatchResult(zonedExpected, localExpected, zonedActual, localActual) {
            @Override
            protected boolean matchZonedZoned() {
                return zonedActual.isBefore(zonedExpected);
            }

            @Override
            protected boolean matchLocalLocal() {
                return localActual.isBefore(localExpected);
            }

            @Override
            protected boolean matchLocalZoned() {
                return zonedActual.toLocalDateTime().isBefore(localExpected);
            }
        };
    }

    public String getBefore() {
        return getValue();
    }
}
