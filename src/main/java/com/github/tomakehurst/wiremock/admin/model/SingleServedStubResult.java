package com.github.tomakehurst.wiremock.admin.model;

import com.github.tomakehurst.wiremock.stubbing.ServedStub;
import com.google.common.base.Optional;

public class SingleServedStubResult extends SingleItemResult<ServedStub> {

    public SingleServedStubResult(ServedStub item) {
        super(item);
    }

    public static SingleServedStubResult fromOptional(Optional<ServedStub> servedStub) {
        return new SingleServedStubResult(servedStub.orNull());
    }
}
