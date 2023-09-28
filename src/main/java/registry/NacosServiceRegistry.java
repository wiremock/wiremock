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
import java.util.List;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;

public class NacosServiceRegistry {

  private final String registerUrl = "http://%s/nacos/v1/ns/instance%s";

  private final NacosDiscoveryProperties nacosDiscoveryProperties;

  public NacosServiceRegistry(NacosDiscoveryProperties nacosDiscoveryProperties) {
    this.nacosDiscoveryProperties = nacosDiscoveryProperties;
  }

  public void registerInstance() {
    String urlParams = new InstanceParam().toInstanceParam(nacosDiscoveryProperties).toUrlParams();
    if (null == urlParams || "".equals(urlParams)) {
      System.out.println(
          "NacosServiceRegistry:registerInstance:urlParams is empty ... stop registry...");
      return;
    }
    String url =
        String.format(registerUrl, nacosDiscoveryProperties.getServerAddress(), "?" + urlParams);
    System.out.println("registerInstance: url:" + url);
    String response = execPostHttp(url);
    System.out.println("NacosServiceRegistry:registerInstance:response is " + response);
  }

  private String execPostHttp(String url) {

    CloseableHttpClient client = null;
    CloseableHttpResponse response = null;
    try {
      client = HttpClients.createDefault();
      HttpPost httpPost = new HttpPost(url);
      httpPost.setConfig(
          RequestConfig.custom()
              .setConnectTimeout(Timeout.ofMinutes(1))
              .setConnectionRequestTimeout(Timeout.ofMinutes(1))
              .build());
      response = client.execute(httpPost);
      return EntityUtils.toString(response.getEntity());
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (client != null) {
          client.close();
        }
        if (response != null) {
          response.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  class InstanceParam {
    private String serviceName;
    private String ip;
    private String port;
    private String namespaceId;
    private String groupName;

    public InstanceParam toInstanceParam(NacosDiscoveryProperties properties) {

      return new InstanceParam(
          properties.getServiceName(),
          properties.getIp(),
          String.valueOf(properties.getPort()),
          properties.getNamespaceId(),
          properties.getGroupName());
    }

    public InstanceParam() {}

    public InstanceParam(
        String serviceName, String ip, String port, String namespaceId, String groupName) {
      this.serviceName = serviceName;
      this.ip = ip;
      this.port = port;
      this.namespaceId = namespaceId;
      this.groupName = groupName;
    }

    public String toUrlParams() {
      List<String> params = new ArrayList<>();
      if (null != serviceName) {
        params.add("serviceName=" + serviceName);
      }
      if (null != ip) {
        params.add("ip=" + ip);
      }
      if (null != port) {
        params.add("port=" + port);
      }
      if (null != namespaceId) {
        params.add("namespaceId=" + namespaceId);
      }
      if (null != groupName) {
        params.add("groupName=" + groupName);
      }
      if (params.size() > 0) {
        return String.join("&", params);
      }
      return "";
    }
  }
}
