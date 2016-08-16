package com.github.tomakehurst.wiremock.admin.model;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.base.Optional;

public class SingleStubMappingResult extends SingleItemResult<StubMapping> {

    public SingleStubMappingResult(StubMapping item) {
        super(item);
    }

    public static SingleStubMappingResult fromOptional(Optional<StubMapping> optional) {
        return new SingleStubMappingResult(optional.orNull());
    }
}
