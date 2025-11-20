package com.github.tomakehurst.wiremock.stubbing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.Metadata;
import com.github.tomakehurst.wiremock.extension.PostServeActionDefinition;
import com.github.tomakehurst.wiremock.extension.ServeEventListenerDefinition;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;

@JsonPropertyOrder({"id", "name", "request", "newRequest", "response"})
@JsonIgnoreProperties({
        "$schema", "uuid"
}) // $schema allows this to be added as a hint to IDEs like VS Code
public record StubMapping(
        UUID id,
        String name,
        Boolean persistent,
        RequestPattern request,
        ResponseDefinition response,
        Integer priority,
        String scenarioName,
        String requiredScenarioState,
        String newScenarioState,
        List<PostServeActionDefinition> postServeActions,
        List<ServeEventListenerDefinition> serveEventListeners,
        Metadata metadata,
        @JsonView(Json.PrivateView.class) long insertionIndex
) implements StubMappingOrMappings {

  public StubMapping(UUID id, String name, Boolean persistent, RequestPattern request, ResponseDefinition response, Integer priority, String scenarioName, String requiredScenarioState, String newScenarioState, List<PostServeActionDefinition> postServeActions, List<ServeEventListenerDefinition> serveEventListeners, Metadata metadata, @JsonView(Json.PrivateView.class) long insertionIndex) {
    this.id = id != null ? id : UUID.randomUUID();
    this.name = name;
    this.persistent = persistent;
    this.request = request;
    this.response = response;
    this.priority = priority;
    this.scenarioName = scenarioName;
    this.requiredScenarioState = requiredScenarioState;
    this.newScenarioState = newScenarioState;
    this.postServeActions = postServeActions;
    this.serveEventListeners = serveEventListeners;
    this.metadata = metadata;
    this.insertionIndex = insertionIndex;
  }

  public static final int DEFAULT_PRIORITY = 5;

  public static final StubMapping NOT_CONFIGURED = StubMapping.builder().setResponse(ResponseDefinition.notConfigured()).build();

  public static Builder builder() {
    return new Builder();
  }

  public Builder thaw() {
    return new Builder(this);
  }

  public StubMapping transform(Consumer<Builder> transformer) {
    final Builder builder = thaw();
    transformer.accept(builder);
    return builder.build();
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Boolean isPersistent() {
    return persistent;
  }

  public RequestPattern getRequest() {
    return getFirstNonNull(request, RequestPattern.ANYTHING);
  }

  public ResponseDefinition getResponse() {
    return getFirstNonNull(response, ResponseDefinition.ok());
  }

  public Integer getPriority() {
    return priority;
  }

  public String getScenarioName() {
    return scenarioName;
  }

  public String getRequiredScenarioState() {
    return requiredScenarioState;
  }

  public String getNewScenarioState() {
    return newScenarioState;
  }

  public List<PostServeActionDefinition> getPostServeActions() {
    return postServeActions;
  }

  public List<ServeEventListenerDefinition> getServeEventListeners() {
    return serveEventListeners;
  }

  public Metadata getMetadata() {
    return metadata;
  }

  public long getInsertionIndex() {
    return insertionIndex;
  }

  @JsonIgnore
  public boolean isInScenario() {
    return scenarioName != null;
  }

  @JsonIgnore
  public boolean modifiesScenarioState() {
    return newScenarioState != null;
  }

  @JsonIgnore
  public boolean isIndependentOfScenarioState() {
    return !isInScenario() || requiredScenarioState == null;
  }

  public int comparePriorityWith(StubMapping otherMapping) {
    int thisPriority = priority != null ? priority : DEFAULT_PRIORITY;
    int otherPriority = otherMapping.priority != null ? otherMapping.priority : DEFAULT_PRIORITY;
    return thisPriority - otherPriority;
  }

  public boolean shouldBePersisted() {
    return persistent != null && persistent;
  }

  @Override
  public List<StubMapping> getMappingOrMappings() {
    return List.of(this);
  }

  @Override
  public boolean isMulti() {
    return false;
  }

  public static class Builder {
    private UUID id = UUID.randomUUID();
    private String name;

    private Boolean persistent;

    private RequestPattern request;
    private ResponseDefinition response;
    private Integer priority;
    private String scenarioName;
    private String requiredScenarioState;
    private String newScenarioState;

    private List<PostServeActionDefinition> postServeActions;

    private List<ServeEventListenerDefinition> serveEventListeners;

    private Metadata metadata;

    private long insertionIndex;
    private boolean isDirty;

    public Builder() {
    }

    public Builder(StubMapping existing) {
      this.id = existing.id();
      this.name = existing.name();
      this.persistent = existing.persistent();
      this.request = existing.request();
      this.response = existing.response();
      this.priority = existing.priority();
      this.scenarioName = existing.scenarioName();
      this.requiredScenarioState = existing.requiredScenarioState();
      this.newScenarioState = existing.newScenarioState();
      this.postServeActions = existing.postServeActions();
      this.serveEventListeners = existing.serveEventListeners();
      this.metadata = existing.metadata();
      this.insertionIndex = existing.insertionIndex();
    }

    public StubMapping build() {
      return new StubMapping(
              id,
              name,
              persistent,
              request,
              response,
              priority,
              scenarioName,
              requiredScenarioState,
              newScenarioState,
              postServeActions,
              serveEventListeners,
              metadata,
              insertionIndex
      );
    }

    public UUID getId() {
      return id;
    }

    public Builder setId(UUID id) {
      this.id = id;
      return this;
    }

    public String getName() {
      return name;
    }

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Boolean getPersistent() {
      return persistent;
    }

    public Builder setPersistent(Boolean persistent) {
      this.persistent = persistent;
      return this;
    }

    public RequestPattern getRequest() {
      return request;
    }

    public Builder setRequest(RequestPattern request) {
      this.request = request;
      return this;
    }

    public ResponseDefinition getResponse() {
      return response;
    }

    public Builder setResponse(ResponseDefinition response) {
      this.response = response;
      return this;
    }

    public Integer getPriority() {
      return priority;
    }

    public Builder setPriority(Integer priority) {
      this.priority = priority;
      return this;
    }

    public String getScenarioName() {
      return scenarioName;
    }

    public Builder setScenarioName(String scenarioName) {
      this.scenarioName = scenarioName;
      return this;
    }

    public String getRequiredScenarioState() {
      return requiredScenarioState;
    }

    public Builder setRequiredScenarioState(String requiredScenarioState) {
      this.requiredScenarioState = requiredScenarioState;
      return this;
    }

    public String getNewScenarioState() {
      return newScenarioState;
    }

    public Builder setNewScenarioState(String newScenarioState) {
      this.newScenarioState = newScenarioState;
      return this;
    }

    public List<PostServeActionDefinition> getPostServeActions() {
      return postServeActions;
    }

    public Builder setPostServeActions(List<PostServeActionDefinition> postServeActions) {
      this.postServeActions = postServeActions;
      return this;
    }

    public List<ServeEventListenerDefinition> getServeEventListeners() {
      return serveEventListeners;
    }

    public Builder setServeEventListeners(List<ServeEventListenerDefinition> serveEventListeners) {
      this.serveEventListeners = serveEventListeners;
      return this;
    }

    public Metadata getMetadata() {
      return metadata;
    }

    public Builder setMetadata(Metadata metadata) {
      this.metadata = metadata;
      return this;
    }

    public long getInsertionIndex() {
      return insertionIndex;
    }

    public Builder setInsertionIndex(long insertionIndex) {
      this.insertionIndex = insertionIndex;
      return this;
    }
  }

  public static void main(String[] args) {
    System.out.println(
            Json.write(StubMapping.builder()
                    .setRequest(WireMock.get("/things").build().request())
                    .setResponse(ResponseDefinitionBuilder.okForJson("[]").build())
                    .setPriority(7)
                    .build()
            )
    );
  }
}
