/*
 * Copyright (C) 2017-2024 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may not a copy of the License at
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

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * This enum is implemented similar to the StringHelpers of handlebars. It is basically a library of
 * all available wiremock helpers
 */
public enum WireMockHelpers implements Helper<Object> {
  xPath {
    private final transient HandlebarsXPathHelper helper = new HandlebarsXPathHelper();

    @Override
    public Object apply(final Object context, final Options options) throws IOException {
      return this.helper.apply(String.valueOf(context), options);
    }
  },
  soapXPath {
    private final transient HandlebarsSoapHelper helper = new HandlebarsSoapHelper();

    @Override
    public Object apply(final Object context, final Options options) throws IOException {
      return this.helper.apply(String.valueOf(context), options);
    }
  },
  jsonPath {
    private final transient HandlebarsJsonPathHelper helper = new HandlebarsJsonPathHelper();

    @Override
    public Object apply(final Object context, final Options options) throws IOException {
      return this.helper.apply(context, options);
    }
  },
  randomValue {
    private final transient HandlebarsRandomValuesHelper helper = new HandlebarsRandomValuesHelper();

    @Override
    public Object apply(final Object context, final Options options) throws IOException {
      return this.helper.apply(null, options);
    }
  },
  hostname {
    private final transient HostnameHelper helper = new HostnameHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return this.helper.apply(context, options);
    }
  },
  date {
    private final transient HandlebarsCurrentDateHelper helper = new HandlebarsCurrentDateHelper();

    @Override
    public Object apply(final Object context, final Options options) throws IOException {
      Date dateContext = context instanceof Date ? (Date) context : null;
      return this.helper.apply(dateContext, options);
    }
  },
  now {
    private final transient HandlebarsCurrentDateHelper helper = new HandlebarsCurrentDateHelper();

    @Override
    public Object apply(final Object context, final Options options) throws IOException {
      return this.helper.apply(null, options);
    }
  },
  parseDate {
    private final transient ParseDateHelper helper = new ParseDateHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply(context.toString(), options);
    }
  },
  truncateDate {
    private final transient TruncateDateTimeHelper helper = new TruncateDateTimeHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply(context, options);
    }
  },

  trim {
    private final transient StringTrimHelper helper = new StringTrimHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply(context, options);
    }
  },

  base64 {
    private final transient Base64Helper helper = new Base64Helper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply(context, options);
    }
  },

  urlEncode {
    private final transient UrlEncodingHelper helper = new UrlEncodingHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply(context, options);
    }
  },

  formData {
    private final transient FormDataHelper helper = new FormDataHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply(context, options);
    }
  },

  regexExtract {
    private final transient RegexExtractHelper helper = new RegexExtractHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply(context, options);
    }
  },

  size {
    private final transient SizeHelper helper = new SizeHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply(context, options);
    }
  },

  pickRandom {
    private final transient PickRandomHelper helper = new PickRandomHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply(context, options);
    }
  },

  randomInt {
    private final transient RandomIntHelper helper = new RandomIntHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply(null, options);
    }
  },

  randomDecimal {
    private final transient RandomDecimalHelper helper = new RandomDecimalHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply(null, options);
    }
  },

  range {
    private final transient RangeHelper helper = new RangeHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply(context, options);
    }
  },

  array {
    private final transient ArrayHelper helper = new ArrayHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply(context, options);
    }
  },

  arrayAdd {
    private final transient ArrayAddHelper helper = new ArrayAddHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply((List<?>) context, options);
    }
  },

  arrayRemove {
    private final transient ArrayRemoveHelper helper = new ArrayRemoveHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply((List<?>) context, options);
    }
  },

  parseJson {
    private final transient ParseJsonHelper helper = new ParseJsonHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply(context, options);
    }
  },

  matches {
    private final transient MatchesRegexHelper helper = new MatchesRegexHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply(context, options);
    }
  },

  contains {
    private final transient ContainsHelper helper = new ContainsHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply(context, options);
    }
  },

  math {
    private final transient MathsHelper helper = new MathsHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply(context, options);
    }
  },

  val {
    private final transient ValHelper helper = new ValHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply(context, options);
    }
  },

  arrayJoin {
    private final transient JoinHelper helper = new JoinHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply(context, options);
    }
  },

  formatJson {
    private final transient FormatJsonHelper helper = new FormatJsonHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply(context, options);
    }
  },

  formatXml {
    private final transient FormatXmlHelper helper = new FormatXmlHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply(context, options);
    }
  },

  toJson {
    private final transient ToJsonHelper helper = new ToJsonHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply(context, options);
    }
  },

  jsonMerge {
    private final transient JsonMergeHelper helper = new JsonMergeHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply(context, options);
    }
  },

  jsonRemove {
    private final transient JsonRemoveHelper helper = new JsonRemoveHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply(context, options);
    }
  },

  jsonArrayAdd {
    private final transient JsonArrayAddHelper helper = new JsonArrayAddHelper();

    @Override
    public Object apply(Object context, Options options) throws IOException {
      return helper.apply(context, options);
    }
  }
}