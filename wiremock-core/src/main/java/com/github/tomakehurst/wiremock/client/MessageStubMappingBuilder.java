/*
 * Copyright (C) 2025-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.client;

import com.github.tomakehurst.wiremock.common.Metadata;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.message.MessageAction;
import com.github.tomakehurst.wiremock.message.MessageStubMapping;
import java.util.Map;
import java.util.UUID;

/** Builder interface for creating message stub mappings using a fluent DSL. */
public interface MessageStubMappingBuilder {

  /**
   * Sets the unique identifier for this message stub mapping.
   *
   * @param id the UUID to assign
   * @return this builder
   */
  MessageStubMappingBuilder withId(UUID id);

  /**
   * Sets a human-readable name for this message stub mapping.
   *
   * @param name the name to assign
   * @return this builder
   */
  MessageStubMappingBuilder withName(String name);

  /**
   * Sets the priority for this message stub mapping. Lower numbers indicate higher priority.
   *
   * @param priority the priority value
   * @return this builder
   */
  MessageStubMappingBuilder atPriority(Integer priority);

  /**
   * Sets a pattern to match the message content.
   *
   * @param messagePattern the pattern to match against incoming messages
   * @return this builder
   */
  MessageStubMappingBuilder withMessageBody(StringValuePattern messagePattern);

  /**
   * Sets metadata for this message stub mapping.
   *
   * @param metadata the metadata map
   * @return this builder
   */
  MessageStubMappingBuilder withMetadata(Map<String, ?> metadata);

  /**
   * Sets metadata for this message stub mapping.
   *
   * @param metadata the metadata object
   * @return this builder
   */
  MessageStubMappingBuilder withMetadata(Metadata metadata);

  /**
   * Sets metadata for this message stub mapping.
   *
   * @param metadata the metadata builder
   * @return this builder
   */
  MessageStubMappingBuilder withMetadata(Metadata.Builder metadata);

  /**
   * Configures the actions to trigger when this stub matches. This method terminates the builder
   * chain and returns the built MessageStubMapping.
   *
   * @param actions the actions to execute when matched
   * @return the built MessageStubMapping
   */
  MessageStubMapping willTriggerActions(MessageAction... actions);

  /**
   * Builds the MessageStubMapping from the current builder state.
   *
   * @return the built MessageStubMapping
   */
  MessageStubMapping build();
}
