package com.github.tomakehurst.wiremock.matching;

import static java.util.Collections.min;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.tomakehurst.wiremock.http.MultiValue;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@JsonDeserialize(as = ExactMatchMultiValuePattern.class)
public class ExactMatchMultiValuePattern extends MultiValuePattern {

  @JsonProperty("hasExactly")
  private List<StringValuePattern> stringValuePatterns;


  @JsonCreator
  public ExactMatchMultiValuePattern(
      @JsonProperty("hasExactly") final List<StringValuePattern> valuePatterns) {
    stringValuePatterns = valuePatterns;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public String getExpected() {
    return null;
  }

  @Override
  public MatchResult match(MultiValue value) {
    List<MatchResult> matchResults = stringValuePatterns.stream()
        .map(stringValuePattern -> getBestMatch(stringValuePattern, value.values()))
        .collect(Collectors.toList());
    matchResults.add(MatchResult.of(stringValuePatterns.size() == value.values().size()));
    return MatchResult.aggregate(matchResults);
  }

  private static MatchResult getBestMatch(final StringValuePattern valuePattern,
      List<String> values) {
    List<MatchResult> allResults = values.stream().map(valuePattern::match)
        .collect(Collectors.toList());
    return min(allResults, Comparator.comparingDouble(MatchResult::getDistance));
  }
}
