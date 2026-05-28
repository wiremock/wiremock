/*
 * Copyright (C) 2026 Thomas Akehurst
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

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.http.Request;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@JsonInclude(NON_NULL)
@NullMarked
public class FixedChannelMessageTrigger implements MessageTrigger {

  private final String providerName;
  private final String channelName;
  @Nullable private final MessagePattern messagePattern;

  @JsonCreator
  public FixedChannelMessageTrigger(
      @JsonProperty("providerName") String providerName,
      @JsonProperty("channelName") String channelName,
      @Nullable @JsonProperty("messagePattern") MessagePattern messagePattern) {
    this.providerName = providerName;
    this.channelName = channelName;
    this.messagePattern = messagePattern;
  }

  public String getProviderName() {
    return providerName;
  }

  public String getChannelName() {
    return channelName;
  }

  @Nullable
  public MessagePattern getMessagePattern() {
    return messagePattern;
  }

  public FixedChannelMessageTrigger withMessagePattern(MessagePattern messagePattern) {
    return new FixedChannelMessageTrigger(providerName, channelName, messagePattern);
  }

  public boolean matches(String providerName, String channelName, Message message) {
    if (!Objects.equals(this.providerName, providerName)
        || !Objects.equals(this.channelName, channelName)) {
      return false;
    }
    return messagePattern == null || messagePattern.matches((Request) null, message);
  }

  @Override
  public boolean equals(Object o) {
    if (getClass() != o.getClass()) return false;
    FixedChannelMessageTrigger that = (FixedChannelMessageTrigger) o;
    return Objects.equals(providerName, that.providerName)
        && Objects.equals(channelName, that.channelName)
        && Objects.equals(messagePattern, that.messagePattern);
  }

  @Override
  public int hashCode() {
    return Objects.hash(providerName, channelName, messagePattern);
  }
}
