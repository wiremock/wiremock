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
package org.wiremock.url;

class BaseUrlParser implements CharSequenceParser<BaseUrl> {

  static final BaseUrlParser INSTANCE = new BaseUrlParser();

  @Override
  public BaseUrl parse(CharSequence url) throws IllegalBaseUrl {
    try {
      var urlReference = UrlReferenceParser.INSTANCE.parse(url);
      if (urlReference instanceof BaseUrl) {
        return (BaseUrl) urlReference;
      } else {
        throw new IllegalBaseUrl(url.toString());
      }
    } catch (IllegalUrlPart illegalUrlPart) {
      throw new IllegalBaseUrl(url.toString(), illegalUrlPart);
    }
  }
}
