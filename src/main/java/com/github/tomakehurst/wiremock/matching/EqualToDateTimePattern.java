package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.DateTimeTruncation;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class EqualToDateTimePattern extends AbstractDateTimePattern {

    public EqualToDateTimePattern(String dateTimeSpec) {
        super(dateTimeSpec, null, (DateTimeTruncation) null, null);
    }

    public EqualToDateTimePattern(
            @JsonProperty("equalToDateTime") String dateTimeSpec,
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
                return zonedActual.isEqual(zonedExpected);
            }

            @Override
            protected boolean matchLocalLocal() {
                return localActual.isEqual(localExpected);
            }

            @Override
            protected boolean matchLocalZoned() {
                return zonedActual.toLocalDateTime().isEqual(localExpected);
            }
        };
    }

    public String getEqualToDateTime() {
        return getValue();
    }
}
