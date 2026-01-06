/*
 * Copyright (C) 2025-2026 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.message;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.client.MessageStubMappingBuilder;
import com.github.tomakehurst.wiremock.common.Metadata;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import org.jspecify.annotations.NonNull;

@JsonInclude(NON_EMPTY)
public class MessageStubMapping {

  public static final int DEFAULT_PRIORITY = 5;

  private final UUID id;
  private final String name;
  private final Integer priority;
  private final MessageTrigger trigger;
  private final List<MessageAction> actions;
  @NonNull private final Metadata metadata;

  @JsonCreator
  public MessageStubMapping(
      @JsonProperty("id") UUID id,
      @JsonProperty("name") String name,
      @JsonProperty("priority") Integer priority,
      @JsonProperty("trigger") MessageTrigger trigger,
      @JsonProperty("actions") List<MessageAction> actions,
      @JsonProperty("metadata") Metadata metadata) {
    this.id = id != null ? id : UUID.randomUUID();
    this.name = name;
    this.priority = priority;
    this.trigger = trigger != null ? trigger : IncomingMessageTrigger.ANYTHING;
    this.actions = actions != null ? List.of(actions) : Collections.emptyList();
    this.metadata = metadata != null ? metadata : new Metadata();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static MessageStubMapping create(Consumer<Builder> transformer) {
    final Builder builder = builder();
    transformer.accept(builder);
    return builder.build();
  }

  public MessageStubMapping transform(Consumer<Builder> transformer) {
    final Builder builder = toBuilder();
    transformer.accept(builder);
    return builder.build();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Integer getPriority() {
    return priority;
  }

  public MessageTrigger getTrigger() {
    return trigger;
  }

  @JsonIgnore
  public RequestPattern getChannelPattern() {
    if (trigger instanceof IncomingMessageTrigger) {
      return ((IncomingMessageTrigger) trigger).getChannelPattern();
    }
    return null;
  }

  @JsonIgnore
  public MessagePattern getMessagePattern() {
    if (trigger instanceof IncomingMessageTrigger) {
      return ((IncomingMessageTrigger) trigger).getMessagePattern();
    }
    return null;
  }

  public List<MessageAction> getActions() {
    return actions;
  }

  @NonNull
  public Metadata getMetadata() {
    return metadata;
  }

  public boolean matches(MessageChannel channel, Message message) {
    if (trigger instanceof IncomingMessageTrigger) {
      return ((IncomingMessageTrigger) trigger).matches(channel, message);
    }
    return false;
  }

  public int comparePriorityWith(MessageStubMapping other) {
    int thisPriority = priority != null ? priority : DEFAULT_PRIORITY;
    int otherPriority = other.priority != null ? other.priority : DEFAULT_PRIORITY;
    return thisPriority - otherPriority;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    MessageStubMapping that = (MessageStubMapping) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "MessageStubMapping{"
        + "id="
        + id
        + ", name='"
        + name
        + '\''
        + ", priority="
        + priority
        + ", trigger="
        + trigger
        + ", actions="
        + actions
        + ", metadata="
        + metadata
        + '}';
  }

  public static class Builder implements MessageStubMappingBuilder {
    private UUID id;
    private String name;
    private Integer priority;
    private RequestPattern channelPattern;
    private ContentPattern<?> bodyPattern;
    private MessageTrigger explicitTrigger;
    private ArrayList<MessageAction> actions = new ArrayList<>();
    @NonNull private Metadata metadata = new Metadata();

    public Builder() {}

    public Builder(MessageStubMapping existing) {
      this.id = existing.id;
      this.name = existing.name;
      this.priority = existing.priority;
      if (existing.trigger instanceof IncomingMessageTrigger incomingTrigger) {
        this.channelPattern = incomingTrigger.getChannelPattern();
        if (incomingTrigger.getMessagePattern() != null) {
          this.bodyPattern = incomingTrigger.getMessagePattern().getBodyPattern();
        }
      } else {
        this.explicitTrigger = existing.trigger;
      }
      this.actions = new ArrayList<>(existing.actions);
      this.metadata = existing.metadata;
    }

    @Override
    public Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    @Override
    public Builder withName(String name) {
      this.name = name;
      return this;
    }

    @Override
    public Builder atPriority(Integer priority) {
      this.priority = priority;
      return this;
    }

    public Builder withPriority(Integer priority) {
      return atPriority(priority);
    }

    @Override
    public Builder withMessageBody(StringValuePattern messagePattern) {
      this.bodyPattern = messagePattern;
      return this;
    }

    public Builder onChannelFromRequestMatching(String urlPath) {
      return onChannelFromRequestMatching(newRequestPattern().withUrl(urlPathEqualTo(urlPath)));
    }

    public Builder onChannelFromRequestMatching(RequestPatternBuilder channelPatternBuilder) {
      this.channelPattern = channelPatternBuilder.build();
      return this;
    }

    public Builder onChannelFromRequestMatching(RequestPattern channelPattern) {
      this.channelPattern = channelPattern;
      return this;
    }

    public Builder withBody(ContentPattern<?> bodyPattern) {
      this.bodyPattern = bodyPattern;
      return this;
    }

    public Builder triggeredByHttpStub(UUID stubId) {
      this.explicitTrigger = HttpStubTrigger.forStubId(stubId);
      return this;
    }

    public Builder triggeredByHttpStub(String stubId) {
      this.explicitTrigger = HttpStubTrigger.forStubId(stubId);
      return this;
    }

    public Builder triggeredByHttpRequest(RequestPattern requestPattern) {
      this.explicitTrigger = HttpRequestTrigger.forRequestPattern(requestPattern);
      return this;
    }

    public Builder triggeredByHttpRequest(RequestPatternBuilder requestPatternBuilder) {
      this.explicitTrigger = HttpRequestTrigger.forRequestPattern(requestPatternBuilder.build());
      return this;
    }

    public Builder withActions(List<MessageAction> actions) {
      this.actions = new ArrayList<>(actions);
      return this;
    }

    @Override
    public MessageStubMapping willTriggerActions(MessageAction... actions) {
      this.actions.addAll(Arrays.asList(actions));
      return build();
    }

    public Builder triggersAction(MessageAction action) {
      this.actions.add(action);
      return this;
    }

    @Override
    public Builder withMetadata(Map<String, ?> metadataMap) {
      this.metadata = new Metadata(metadataMap);
      return this;
    }

    @Override
    public Builder withMetadata(Metadata metadata) {
      this.metadata = metadata;
      return this;
    }

    @Override
    public Builder withMetadata(Metadata.Builder metadata) {
      this.metadata = metadata.build();
      return this;
    }

    @Override
    public MessageStubMapping build() {
      MessageTrigger trigger;
      if (explicitTrigger != null) {
        trigger = explicitTrigger;
      } else {
        MessagePattern messagePattern =
            bodyPattern != null ? new MessagePattern(null, bodyPattern) : null;
        trigger = new IncomingMessageTrigger(channelPattern, messagePattern);
      }
      return new MessageStubMapping(id, name, priority, trigger, actions, metadata);
    }
  }
}
