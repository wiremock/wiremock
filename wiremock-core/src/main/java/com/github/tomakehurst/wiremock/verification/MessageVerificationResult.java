/*
 * Copyright (C) 2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.verification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.Json;

public class MessageVerificationResult {

  private final int count;
  private final boolean messageJournalDisabled;

  @JsonCreator
  public MessageVerificationResult(
      @JsonProperty("count") int count,
      @JsonProperty("messageJournalDisabled") boolean messageJournalDisabled) {
    this.count = count;
    this.messageJournalDisabled = messageJournalDisabled;
  }

  public static MessageVerificationResult from(String json) {
    return Json.read(json, MessageVerificationResult.class);
  }

  public static MessageVerificationResult withCount(int count) {
    return new MessageVerificationResult(count, false);
  }

  public static MessageVerificationResult withMessageJournalDisabled() {
    return new MessageVerificationResult(-1, true);
  }

  public int getCount() {
    return count;
  }

  public boolean isMessageJournalDisabled() {
    return messageJournalDisabled;
  }

  public void assertMessageJournalEnabled() {
    if (messageJournalDisabled) {
      throw new MessageJournalDisabledException();
    }
  }
}

