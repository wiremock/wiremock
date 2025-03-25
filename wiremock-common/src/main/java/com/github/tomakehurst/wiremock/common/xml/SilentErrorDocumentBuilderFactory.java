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
package com.github.tomakehurst.wiremock.common.xml;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

import com.github.tomakehurst.wiremock.common.SilentErrorHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

class SilentErrorDocumentBuilderFactory extends DelegateDocumentBuilderFactory {

  SilentErrorDocumentBuilderFactory(DocumentBuilderFactory delegate) {
    super(delegate);
  }

  @Override
  public DocumentBuilder newDocumentBuilder() {
    try {
      DocumentBuilder documentBuilder = delegate.newDocumentBuilder();
      documentBuilder.setErrorHandler(new SilentErrorHandler());
      return documentBuilder;
    } catch (ParserConfigurationException e) {
      return throwUnchecked(e, DocumentBuilder.class);
    }
  }
}
