package com.github.tomakehurst.wiremock.verification;

import static java.util.Collections.emptyList;

import java.util.List;

import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMappings;

public class NullNearMissCalculator extends NearMissCalculator {

    public NullNearMissCalculator() {
        super(null, null);
    }

    @Override
    public List<NearMiss> findNearestTo(LoggedRequest request) {
        return emptyList();
    }

    @Override
    public List<NearMiss> findNearestTo(RequestPattern requestPattern) {
        return emptyList();
    }

    
}
