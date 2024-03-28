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
package com.github.tomakehurst.wiremock.extension.responsetemplating;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.tomakehurst.wiremock.common.Exceptions;
import com.github.tomakehurst.wiremock.common.RequestCache;
import java.io.IOException;
import java.io.Writer;
import org.apache.commons.io.output.StringBuilderWriter;

public class HandlebarsOptimizedTemplate {

  private final Template template;

  private String startContent;
  private String templateContent;
  private String endContent;

  public HandlebarsOptimizedTemplate(final Handlebars handlebars, final String content) {
    startContent = content;
    templateContent = "";
    endContent = "";

    int firstDelimStartPosition = content.indexOf(Handlebars.DELIM_START);
    if (firstDelimStartPosition != -1) {
      int lastDelimEndPosition = content.lastIndexOf(Handlebars.DELIM_END);
      if (lastDelimEndPosition != -1) {
        startContent = content.substring(0, firstDelimStartPosition);
        templateContent =
            content.substring(
                firstDelimStartPosition, lastDelimEndPosition + Handlebars.DELIM_END.length());
        endContent =
            content.substring(
                lastDelimEndPosition + Handlebars.DELIM_END.length(), content.length());
      }
    }

    this.template = uncheckedCompileTemplate(handlebars, templateContent);
  }

  private static Template uncheckedCompileTemplate(Handlebars handlebars, String templateContent) {
    try {
      return handlebars.compileInline(templateContent);
    } catch (IOException e) {
      return Exceptions.throwUnchecked(e, Template.class);
    }
  }

  public String apply(Object contextData) {
    final RequestCache requestCache = RequestCache.getCurrent();
    Context context = Context.newBuilder(contextData).combine("requestCache", requestCache).build();

    return startContent + applyTemplate(context) + endContent;
  }

  private String applyTemplate(Context context) {
    return Exceptions.uncheck(
        () -> {
          Writer stringWriter = new StringBuilderWriter(template.text().length() * 2);
          template.apply(context, stringWriter);
          return stringWriter.toString();
        },
        String.class);
  }
}
