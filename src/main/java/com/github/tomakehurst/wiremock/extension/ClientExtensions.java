/*
 * Copyright (C) 2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension;

import java.util.List;

public class ClientExtensions extends Extensions {

  private final boolean extensionScanningEnabled;

  public ClientExtensions(
      ExtensionDeclarations extensionDeclarations, boolean extensionScanningEnabled) {
    super(extensionDeclarations);
    this.extensionScanningEnabled = extensionScanningEnabled;
  }

  @Override
  public void load() {
    loadExtensions(extensionScanningEnabled);
  }

  @Override
  protected List<Extension> loadFactory(ExtensionFactory factory) {
    return factory.createForClient();
  }
}
