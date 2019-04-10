package com.github.tomakehurst.wiremock.extension;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;

public interface StubLifecycleListener extends Extension {

    void stubCreated(StubMapping stub);
    void stubEdited(StubMapping stub);
    void stubRemoved(StubMapping stub);
    void stubsReset();
    void stubsResetToDefaults();
}
