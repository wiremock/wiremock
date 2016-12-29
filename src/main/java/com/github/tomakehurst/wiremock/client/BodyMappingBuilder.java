package com.github.tomakehurst.wiremock.client;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.BodyRequestPattern;
import com.github.tomakehurst.wiremock.matching.BodyRequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.FormFieldPattern;
import com.github.tomakehurst.wiremock.matching.RequestMatcher;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.Map;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.newLinkedHashMap;

public class BodyMappingBuilder implements ScenarioMappingBuilder {
    private BodyRequestPatternBuilder requestPatternBuilder;
    private ResponseDefinitionBuilder responseDefBuilder;
    private Integer priority;
    private String scenarioName;
    private String requiredScenarioState;
    private String newScenarioState;
    private UUID id;
    private boolean isPersistent = false;
    private Map<String, Parameters> postServeActions = newLinkedHashMap();

    BodyMappingBuilder(RequestMethod method, UrlPattern urlPattern) {
        requestPatternBuilder = new BodyRequestPatternBuilder(method, urlPattern);
    }

    BodyMappingBuilder(RequestMatcher requestMatcher) {
        requestPatternBuilder = new BodyRequestPatternBuilder(requestMatcher);
    }

    BodyMappingBuilder(String customRequestMatcherName, Parameters parameters) {
        requestPatternBuilder = new BodyRequestPatternBuilder(customRequestMatcherName, parameters);
    }

    @Override
    public BodyMappingBuilder willReturn(ResponseDefinitionBuilder responseDefBuilder) {
        this.responseDefBuilder = responseDefBuilder;
        return this;
    }

    @Override
    public BodyMappingBuilder atPriority(Integer priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public BodyMappingBuilder withHeader(String key, StringValuePattern headerPattern) {
        requestPatternBuilder.withHeader(key, headerPattern);
        return this;
    }

    @Override
    public BodyMappingBuilder withCookie(String name, StringValuePattern cookieValuePattern) {
        requestPatternBuilder.withCookie(name, cookieValuePattern);
        return this;
    }

    @Override
    public <P> ScenarioMappingBuilder withPostServeAction(String extensionName, P parameters) {
        Parameters params = parameters instanceof Parameters ?
                (Parameters) parameters :
                Parameters.of(parameters);
        postServeActions.put(extensionName, params);
        return this;
    }

    @Override
    public BodyMappingBuilder withQueryParam(String key, StringValuePattern queryParamPattern) {
        requestPatternBuilder.withQueryParam(key, queryParamPattern);
        return this;
    }

    @Override
    public BodyMappingBuilder withRequestBody(StringValuePattern bodyPattern) {
        requestPatternBuilder.withRequestBody(bodyPattern);
        return this;
    }

    @Override
    public BodyMappingBuilder inScenario(String scenarioName) {
        checkArgument(scenarioName != null, "Scenario name must not be null");

        this.scenarioName = scenarioName;
        return this;
    }

    @Override
    public BodyMappingBuilder whenScenarioStateIs(String stateName) {
        this.requiredScenarioState = stateName;
        return this;
    }

    @Override
    public BodyMappingBuilder willSetStateTo(String stateName) {
        this.newScenarioState = stateName;
        return this;
    }

    @Override
    public BodyMappingBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    @Override
    public ScenarioMappingBuilder persistent() {
        this.isPersistent = true;
        return this;
    }

    @Override
    public BodyMappingBuilder withBasicAuth(String username, String password) {
        requestPatternBuilder.withBasicAuth(new BasicCredentials(username, password));
        return this;
    }

    /**
     * @see BodyRequestPattern
     */
    public BodyMappingBuilder withFormParam(StringValuePattern key, StringValuePattern value) {
        requestPatternBuilder.withFormParam(new FormFieldPattern(key, value));
        return this;
    }

    /**
     * @see BodyRequestPattern
     */
    public BodyMappingBuilder withFormParam(String key, String value) {
        requestPatternBuilder.withFormParam(new FormFieldPattern(new EqualToPattern(key), new EqualToPattern(value)));
        return this;
    }

    @Override
    public StubMapping build() {
        if (scenarioName == null && (requiredScenarioState != null || newScenarioState != null)) {
            throw new IllegalStateException("Scenario name must be specified to require or set a new scenario state");
        }
        BodyRequestPattern requestPattern = requestPatternBuilder.build();
        ResponseDefinition response = responseDefBuilder.build();
        StubMapping mapping = new StubMapping(requestPattern, response);
        mapping.setPriority(priority);
        mapping.setScenarioName(scenarioName);
        mapping.setRequiredScenarioState(requiredScenarioState);
        mapping.setNewScenarioState(newScenarioState);
        mapping.setUuid(id);
        mapping.setPersistent(isPersistent);

        mapping.setPostServeActions(postServeActions.isEmpty() ? null : postServeActions);

        return mapping;
    }

}
