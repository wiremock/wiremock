package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RandomHelperTest extends HandlebarsHelperTestBase {

  RandomHelper helper;

  @BeforeEach
  public void init() {
    helper = new RandomHelper();
  }

  @Test
  public void rendersAMeaningfulErrorWhenExpressionIsInvalid() {
    testHelperError(
            helper,
            "something really random",
            "umm",
            is("[ERROR: Unable to evaluate the expression something really random]"));
  }

  @Test
  public void returnsRandomValue() throws Exception {
    assertThat(renderHelperValue(helper, "Name.first_name"), is(any(String.class)));
  }
}
