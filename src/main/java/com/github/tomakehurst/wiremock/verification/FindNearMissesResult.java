package com.github.tomakehurst.wiremock.verification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FindNearMissesResult {

    private final List<NearMiss> nearMisses;

    @JsonCreator
    public FindNearMissesResult(@JsonProperty("nearMisses") List<NearMiss> nearMisses) {
        this.nearMisses = nearMisses;
    }

    public List<NearMiss> getNearMisses() {
        return nearMisses;
    }
}
