package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.DateTimeOffset;
import com.github.tomakehurst.wiremock.common.DateTimeTruncation;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class AfterDateTimePattern extends AbstractDateTimePattern {

    public AfterDateTimePattern(String dateTimeSpec) {
        super(dateTimeSpec);
    }

    public AfterDateTimePattern(
            @JsonProperty("after") String dateTimeSpec,
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
                return zonedActual.isAfter(zonedExpected);
            }

            @Override
            protected boolean matchLocalLocal() {
                return localActual.isAfter(localExpected);
            }

            @Override
            protected boolean matchLocalZoned() {
                return zonedActual.toLocalDateTime().isAfter(localExpected);
            }
        };
    }

    public String getAfter() {
        return getValue();
    }
}
