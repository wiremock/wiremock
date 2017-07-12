package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.ResponseDefinitionBodyMatcher;
import com.github.tomakehurst.wiremock.admin.model.SnapshotStubMappingBodyExtractor;
import com.github.tomakehurst.wiremock.admin.model.SnapshotStubMappingPostProcessor;
import com.github.tomakehurst.wiremock.admin.model.SnapshotStubMappingTransformerRunner;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SnapshotStubMappingPostProcessorTest {
    private static final List<StubMapping> TEST_STUB_MAPPINGS = ImmutableList.of(
        aMapping("/foo"),
        aMapping("/bar"),
        aMapping("/foo")
    );

    @Test
    public void processFiltersRepeatedRequestsWhenNotRecordingScenarios() {
        final List<StubMapping> actual = new SnapshotStubMappingPostProcessor(
            false, noopTransformerRunner(), null, null
        ).process(TEST_STUB_MAPPINGS);

        assertThat(actual, hasSize(2));
        assertThat(actual.get(0).getRequest().getUrl(), equalTo("/foo"));
        assertThat(actual.get(1).getRequest().getUrl(), equalTo("/bar"));
    }

    @Test
    public void processCreatesScenariosForRepeatedRequestsWhenEnabled() {
        final List<StubMapping> actual = new SnapshotStubMappingPostProcessor(
            true, noopTransformerRunner(), null, null
        ).process(TEST_STUB_MAPPINGS);

        assertThat(actual, hasSize(3));
        assertThat(actual.get(0).getRequest().getUrl(), equalTo("/foo"));
        assertThat(actual.get(0).getScenarioName(), equalTo("scenario-foo"));
        assertThat(actual.get(0).getRequiredScenarioState(), is(Scenario.STARTED));
        assertThat(actual.get(0).getNewScenarioState(), is(nullValue()));

        assertThat(actual.get(1).getRequest().getUrl(), equalTo("/bar"));
        assertThat(actual.get(1).getScenarioName(), is(nullValue()));
        assertThat(actual.get(1).getRequiredScenarioState(), is(nullValue()));

        assertThat(actual.get(2).getRequest().getUrl(), equalTo("/foo"));
        assertThat(actual.get(2).getScenarioName(), equalTo("scenario-foo"));
        assertThat(actual.get(2).getRequiredScenarioState(), is(Scenario.STARTED));
        assertThat(actual.get(2).getNewScenarioState(), equalTo("scenario-foo-2"));
    }

    @Test
    public void processRunsTransformers() {
        SnapshotStubMappingTransformerRunner transformerRunner = new SnapshotStubMappingTransformerRunner(null) {
            @Override
            public StubMapping apply(StubMapping stubMapping) {
                // Return StubMapping with "/transformed" at the end of the original URL
                String url = stubMapping.getRequest().getUrl();
                return new StubMapping(
                    newRequestPattern().withUrl(url + "/transformed").build(),
                    ResponseDefinition.ok()
                );
            }
        };

        final List<StubMapping> actual = new SnapshotStubMappingPostProcessor(
            false, transformerRunner, null, null
        ).process(TEST_STUB_MAPPINGS);

        assertThat(actual, hasSize(2));
        assertThat(actual.get(0).getRequest().getUrl(), equalTo("/foo/transformed"));
        assertThat(actual.get(1).getRequest().getUrl(), equalTo("/bar/transformed"));
    }

    @Test
    public void processExtractsBodiesWhenMatched() {
        ResponseDefinitionBodyMatcher bodyMatcher = new ResponseDefinitionBodyMatcher(0, 0) {
            @Override
            public MatchResult match(ResponseDefinition responseDefinition) {
                // Only match the second stub mapping
                return responseDefinition == TEST_STUB_MAPPINGS.get(1).getResponse() ?
                    MatchResult.exactMatch() :
                    MatchResult.noMatch();
            }
        };

        SnapshotStubMappingBodyExtractor bodyExtractor = new SnapshotStubMappingBodyExtractor(null) {
            @Override
            public void extractInPlace(StubMapping stubMapping) {
                stubMapping.setResponse(ResponseDefinition.noContent());
            }
        };

        final List<StubMapping> actual = new SnapshotStubMappingPostProcessor(
            false,
            noopTransformerRunner(),
            bodyMatcher,
            bodyExtractor
        ).process(TEST_STUB_MAPPINGS);

        assertThat(actual, hasSize(2));
        // Should've only modified second stub mapping
        assertThat(actual.get(0).getResponse(), equalTo(ResponseDefinition.ok()));
        assertThat(actual.get(1).getResponse(), equalTo(ResponseDefinition.noContent()));
    }

    private static SnapshotStubMappingTransformerRunner noopTransformerRunner() {
        return new SnapshotStubMappingTransformerRunner(null) {
            @Override
            public StubMapping apply(StubMapping stubMapping) {
                return stubMapping;
            }
        };
    }

    private static StubMapping aMapping(String url) {
        return new StubMapping(
            newRequestPattern().withUrl(url).build(),
            ResponseDefinition.ok()
        );
    }
}
