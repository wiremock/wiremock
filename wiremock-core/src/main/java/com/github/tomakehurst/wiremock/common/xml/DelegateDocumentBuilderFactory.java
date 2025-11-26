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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

abstract class DelegateDocumentBuilderFactory extends DocumentBuilderFactory {
  protected final DocumentBuilderFactory delegate;

  DelegateDocumentBuilderFactory(DocumentBuilderFactory delegate) {
    this.delegate = delegate;
  }

  @Override
  public void setAttribute(String name, Object value) throws IllegalArgumentException {
    delegate.setAttribute(name, value);
  }

  @Override
  public Object getAttribute(String name) throws IllegalArgumentException {
    return delegate.getAttribute(name);
  }

  @Override
  public void setFeature(String name, boolean value) throws ParserConfigurationException {
    delegate.setFeature(name, value);
  }

  @Override
  public boolean getFeature(String name) throws ParserConfigurationException {
    return delegate.getFeature(name);
  }
}
