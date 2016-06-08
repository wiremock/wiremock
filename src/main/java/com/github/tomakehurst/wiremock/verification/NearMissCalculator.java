package com.github.tomakehurst.wiremock.verification;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.NearMiss;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.stubbing.StubMappings;
import com.google.common.base.Function;

import java.util.Comparator;
import java.util.List;

import static com.google.common.collect.FluentIterable.from;
import static java.lang.Math.min;

public class NearMissCalculator {

    public static final int NEAR_MISS_COUNT = 3;

    private final StubMappings stubMappings;
    private final RequestJournal requestJournal;

    public NearMissCalculator(StubMappings stubMappings, RequestJournal requestJournal) {
        this.stubMappings = stubMappings;
        this.requestJournal = requestJournal;
    }

    public List<NearMiss> findNearestFor(final LoggedRequest request) {
        List<StubMapping> allMappings = stubMappings.getAll();
        return from(allMappings).transform(new Function<StubMapping, NearMiss>() {
            public NearMiss apply(StubMapping stubMapping) {
                MatchResult matchResult = stubMapping.getRequest().match(request);
                return new NearMiss(request, stubMapping, matchResult);
            }
        }).toSortedList(new Comparator<NearMiss>() {
            public int compare(NearMiss o1, NearMiss o2) {
                return o1.compareTo(o2);
            }
        }).subList(0, min(NEAR_MISS_COUNT, allMappings.size()));
    }

    public List<NearMiss> findNearestForAllRequests() {
        return null;
    }
}
