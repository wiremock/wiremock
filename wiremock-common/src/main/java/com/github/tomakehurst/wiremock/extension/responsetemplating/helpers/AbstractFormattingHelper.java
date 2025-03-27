/*
 * Copyright (C) 2024-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.TagType;
import java.io.IOException;
import java.util.Arrays;

public abstract class AbstractFormattingHelper extends HandlebarsHelper<Object> {

  abstract String getName();

  abstract String getDataFormat();

  @Override
  public Object apply(Object context, Options options) throws IOException {
    // get the trimmed contents and make sure it isn't empty
    String bodyText;
    if (options.tagType == TagType.SECTION) {
      bodyText = options.fn().toString().trim();
    } else if (context instanceof CharSequence) {
      bodyText = context.toString().trim();
    } else {
      bodyText = "";
    }

    if (bodyText.isEmpty()) {
      return handleError(
          String.format(
              "%s should take a block of %s to format or a single parameter of type String",
              getName(), getDataFormat()));
    }

    // get the format field and default to pretty
    Object formatOption = options.hash.get("format");
    Format format;
    if (formatOption == null) {
      format = Format.pretty;
    } else if (formatOption instanceof Format) {
      format = (Format) formatOption;
    } else if (formatOption instanceof CharSequence) {
      try {
        format = Format.valueOf(formatOption.toString());
      } catch (IllegalArgumentException e) {
        return handleError(
            String.format(
                "%s: format [%s] should be one of %s",
                getName(), formatOption, Arrays.toString(Format.values())));
      }
    } else {
      return handleError(
          String.format(
              "%s: format [%s] of type [%s should be a %s or a String and one of %s]",
              getName(),
              formatOption,
              formatOption.getClass().getName(),
              Format.class.getSimpleName(),
              Arrays.toString(Format.values())));
    }

    return apply(bodyText, format);
  }

  protected abstract String apply(String bodyText, Format format);

  public enum Format {
    pretty,
    compact,
  }
}
