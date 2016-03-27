package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;

public class NearMiss implements Comparable<NearMiss> {

    private final StubMapping mapping;
    private final MatchResult matchResult;

    public NearMiss(StubMapping mapping, MatchResult matchResult) {
        this.mapping = mapping;
        this.matchResult = matchResult;
    }

    public StubMapping getStubMapping() {
        return mapping;
    }

    public MatchResult getMatchResult() {
        return matchResult;
    }

    @Override
    public int compareTo(NearMiss o) {
        return o.getMatchResult().compareTo(matchResult);
    }
}
