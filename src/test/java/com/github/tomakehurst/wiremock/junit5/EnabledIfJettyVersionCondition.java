/*
 * Copyright (C) 2019-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.junit5;

import java.lang.reflect.AnnotatedElement;
import org.eclipse.jetty.util.Jetty;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class EnabledIfJettyVersionCondition implements ExecutionCondition {
  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
    final AnnotatedElement element = context.getElement().orElseThrow(IllegalStateException::new);

    final EnabledIfJettyVersion annotation = element.getAnnotation(EnabledIfJettyVersion.class);
    if (annotation == null) {
      return ConditionEvaluationResult.enabled("@EnabledIfJettyVersion is not present");
    }

    final int major = annotation.major();
    if (Jetty.VERSION.startsWith(major + ".")) {
      return ConditionEvaluationResult.enabled(
          "The Jetty version " + Jetty.VERSION + " matches major version " + major + " vesion");
    } else {
      return ConditionEvaluationResult.disabled(
          "The Jetty version " + Jetty.VERSION + " does not match major " + major + " vesion");
    }
  }
}
