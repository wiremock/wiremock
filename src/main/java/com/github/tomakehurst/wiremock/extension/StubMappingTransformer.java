package com.github.tomakehurst.wiremock.extension;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

/**
 * Base class for stub mapping transformer extensions. This allows transforming stub mappings recorded via the
 * snapshot API endpoint.
 *
 * @see com.github.tomakehurst.wiremock.admin.tasks.SnapshotTask
 */
public abstract class StubMappingTransformer implements Extension {
    public abstract StubMapping transform(StubMapping stubMapping, FileSource files, Parameters parameters);

    public boolean applyGlobally() {
        return true;
    }
}
