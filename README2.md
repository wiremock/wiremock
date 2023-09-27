## 扩展注册到nacos功能
```shell
启动参数：

# nacos.discovery.server-addr: nacos地址
# nacos.discovery.namespace:nacos命名空间
# nacos.discovery.group：nacos分组
# port：服务端口号
# ip ：服务ip

java -jar wiremock-standalone-3.2.0.jar \
--nacos.discovery.server-addr=127.0.0.1:8848 --nacos.discovery.namespace=namespace --nacos.discovery.group=group --ip=127.0.0.1 --port=8080


```