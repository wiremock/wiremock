package com.github.tomakehurst.wiremock.junit;

import com.github.tomakehurst.wiremock.client.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

import java.util.List;

public interface Verifying {

    void verify(RequestPatternBuilder requestPatternBuilder);
    void verify(int count, RequestPatternBuilder requestPatternBuilder);

    List<LoggedRequest> findAll(RequestPatternBuilder requestPatternBuilder);

}
