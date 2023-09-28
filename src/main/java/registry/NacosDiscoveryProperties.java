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
package registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class NacosDiscoveryProperties {

  private String serviceName;

  private String ip;

  private int port;

  private String serverAddress;

  private String namespaceId;

  private String groupName;

  private static List<String> fileNames =
      Arrays.asList(
          "port",
          "ip",
          "nacos.discovery.server-addr",
          "nacos.discovery.namespace",
          "nacos.discovery.group",
          "application.name");

  public NacosDiscoveryProperties() {}

  public NacosDiscoveryProperties(
      String serviceName, String ip, int port, String namespaceId, String groupName) {
    this.serviceName = serviceName;
    this.ip = ip;
    this.port = port;
    this.namespaceId = namespaceId;
    this.groupName = groupName;
  }

  public void parse(String... args) {
    if (null == args || args.length == 0) {
      System.out.println("NacosDiscoveryProperties:parse:args is empty...");
      return;
    }
    for (int i = 0; i < args.length; i++) {
      if (null == args[i]) {
        continue;
      }
      String[] split = args[i].split("=");
      if (split.length != 2) {
        System.err.println("NacosDiscoveryProperties:parse:" + args[i] + "is not vaild...");
        continue;
      }
      Consumer<Object> consumer = valueSetConsumer(split[0].replace("--", ""), split[1]);
      if (null != consumer) {
        consumer.accept(split[1]);
      }
    }
  }

  public static String[] removePrivate(String[] source, String... excludes) {

    List<String> strings = new ArrayList<>();

    List<String> excludeList = Arrays.asList(excludes);
    if (null == source) {
      return new String[] {};
    }
    for (int i = 0; i < source.length; i++) {
      if (null == source[i]) {
        continue;
      }
      String[] split = source[i].split("=");
      String key = split[0].replace("--", "");
      if (split.length == 2 && !excludeList.contains(key) && fileNames.contains(key)) {
        continue;
      }
      strings.add(source[i]);
    }

    return strings.toArray(new String[] {});
  }

  public boolean necessaryPropertiesIsComplete() {
    if (null == serverAddress || 0 == port || null == serviceName) {
      System.out.println(
          "NacosDiscoveryProperties:necessaryPropertiesIsComplete is false... toString is " + this);
      return false;
    }
    return true;
  }

  private Consumer<Object> valueSetConsumer(String key, Object value) {
    if (null == key) {
      System.out.println("NacosDiscoveryProperties:valueSetConsumer:key is null...");
      return null;
    }
    if (null == value) {
      System.out.println("NacosDiscoveryProperties:valueSetConsumer:value is null...");
      return null;
    }
    if ("port".equals(key)) {
      return o -> this.setPort(Integer.parseInt(String.valueOf(value)));
    }
    if ("ip".equals(key)) {
      return o -> this.setIp(String.valueOf(value));
    }
    if ("nacos.discovery.server-addr".equals(key)) {
      return o -> this.setServerAddress(String.valueOf(value));
    }
    if ("nacos.discovery.namespace".equals(key)) {
      return o -> this.setNamespaceId(String.valueOf(value));
    }
    if ("nacos.discovery.group".equals(key)) {
      return o -> this.setGroupName(String.valueOf(value));
    }
    if ("application.name".equals(key)) {
      return o -> this.setServiceName(String.valueOf(value));
    }

    return null;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getNamespaceId() {
    return namespaceId;
  }

  public void setNamespaceId(String namespaceId) {
    this.namespaceId = namespaceId;
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public String getServerAddress() {
    return serverAddress;
  }

  public void setServerAddress(String serverAddress) {
    this.serverAddress = serverAddress;
  }

  @Override
  public String toString() {
    return "NacosDiscoveryProperties{"
        + "serviceName='"
        + serviceName
        + '\''
        + ", ip='"
        + ip
        + '\''
        + ", port="
        + port
        + ", serverAddress='"
        + serverAddress
        + '\''
        + ", namespaceId='"
        + namespaceId
        + '\''
        + ", groupName='"
        + groupName
        + '\''
        + '}';
  }
}
