/*
 * Copyright (C) 2023 Thomas Akehurst
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
package wiremock;

import com.github.tomakehurst.wiremock.standalone.WireMockServerRunner;
import registry.NacosDiscoveryProperties;
import registry.NacosServiceRegistry;
import registry.NacosServiceRegistryManager;

public class Run extends WireMockServerRunner {

  public static void main(String... args) {
    String[] wireMockArgs = NacosDiscoveryProperties.removePrivate(args, new String[] {"port"});
    new Run().run(wireMockArgs);
    NacosDiscoveryProperties nacosDiscoveryProperties = new NacosDiscoveryProperties();
    nacosDiscoveryProperties.parse(args);
    if (0 == nacosDiscoveryProperties.getPort()) {
      nacosDiscoveryProperties.setPort(8080);
    }
    if (nacosDiscoveryProperties.necessaryPropertiesIsComplete()) {
      NacosServiceRegistryManager.heartbeatRegistry(
          new NacosServiceRegistry(nacosDiscoveryProperties));
    } else {
      System.out.println("Run:necessaryPropertiesIsComplete is false...");
    }
  }
}
