/*
 * Copyright (C) 2013-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.junit;

import com.github.tomakehurst.wiremock.client.CountMatchingStrategy;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.NearMiss;
import java.util.List;
import java.util.UUID;

/** The interface Stubbing. */
public interface Stubbing {

  /**
   * Given that stub mapping.
   *
   * @param mappingBuilder the mapping builder
   * @return the stub mapping
   */
  StubMapping givenThat(MappingBuilder mappingBuilder);

  /**
   * Stub for stub mapping.
   *
   * @param mappingBuilder the mapping builder
   * @return the stub mapping
   */
  StubMapping stubFor(MappingBuilder mappingBuilder);

  /**
   * Edit stub.
   *
   * @param mappingBuilder the mapping builder
   */
  void editStub(MappingBuilder mappingBuilder);

  /**
   * Remove stub.
   *
   * @param mappingBuilder the mapping builder
   */
  void removeStub(MappingBuilder mappingBuilder);

  /**
   * Remove stub.
   *
   * @param mappingBuilder the mapping builder
   */
  void removeStub(StubMapping mappingBuilder);

  /**
   * Remove stub.
   *
   * @param id the id
   */
  void removeStub(UUID id);

  /**
   * Gets stub mappings.
   *
   * @return the stub mappings
   */
  List<StubMapping> getStubMappings();

  /**
   * Gets single stub mapping.
   *
   * @param id the id
   * @return the single stub mapping
   */
  StubMapping getSingleStubMapping(UUID id);

  /**
   * Find stub mappings by metadata list.
   *
   * @param pattern the pattern
   * @return the list
   */
  List<StubMapping> findStubMappingsByMetadata(StringValuePattern pattern);

  /**
   * Remove stub mappings by metadata.
   *
   * @param pattern the pattern
   */
  void removeStubMappingsByMetadata(StringValuePattern pattern);

  /**
   * Verify.
   *
   * @param requestPatternBuilder the request pattern builder
   */
  void verify(RequestPatternBuilder requestPatternBuilder);

  /**
   * Verify.
   *
   * @param count the count
   * @param requestPatternBuilder the request pattern builder
   */
  void verify(int count, RequestPatternBuilder requestPatternBuilder);

  /**
   * Verify.
   *
   * @param countMatchingStrategy the count matching strategy
   * @param requestPatternBuilder the request pattern builder
   */
  void verify(
      CountMatchingStrategy countMatchingStrategy, RequestPatternBuilder requestPatternBuilder);

  /**
   * Find all list.
   *
   * @param requestPatternBuilder the request pattern builder
   * @return the list
   */
  List<LoggedRequest> findAll(RequestPatternBuilder requestPatternBuilder);

  /**
   * Gets all serve events.
   *
   * @return the all serve events
   */
  List<ServeEvent> getAllServeEvents();

  /**
   * Sets global fixed delay.
   *
   * @param milliseconds the milliseconds
   */
  void setGlobalFixedDelay(int milliseconds);

  /**
   * Find all unmatched requests list.
   *
   * @return the list
   */
  List<LoggedRequest> findAllUnmatchedRequests();

  /**
   * Find near misses for all unmatched requests list.
   *
   * @return the list
   */
  List<NearMiss> findNearMissesForAllUnmatchedRequests();

  /**
   * Find near misses for list.
   *
   * @param loggedRequest the logged request
   * @return the list
   */
  List<NearMiss> findNearMissesFor(LoggedRequest loggedRequest);

  /**
   * Find all near misses for list.
   *
   * @param requestPatternBuilder the request pattern builder
   * @return the list
   */
  List<NearMiss> findAllNearMissesFor(RequestPatternBuilder requestPatternBuilder);
}
