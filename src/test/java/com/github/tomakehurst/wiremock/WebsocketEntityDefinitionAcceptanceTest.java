/*
 * Copyright (C) 2025-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.binaryEntity;
import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.message;
import static com.github.tomakehurst.wiremock.client.WireMock.messageStubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.sendMessage;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.nio.file.Files.write;
import static java.nio.file.Files.writeString;
import static java.util.concurrent.TimeUnit.SECONDS;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.awaitility.Awaitility.waitAtMost;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.message.MessageStubMapping;
import com.github.tomakehurst.wiremock.testsupport.WebsocketTestClient;
import java.io.File;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class WebsocketEntityDefinitionAcceptanceTest extends WebsocketAcceptanceTestBase {

  @Test
  void textEntityDefinitionWithStringDataResolvesToString() {
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("String data stub")
            .withBody(equalTo("trigger"))
            .triggersAction(sendMessage().withBody("hello world").onOriginatingChannel())
            .build();
    wireMockServer.addMessageStubMapping(stub);

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/string-data-test");

    String response = testClient.sendMessageAndWaitForResponse(url, "trigger");
    assertThat(response, is("hello world"));
  }

  //  @Test
  //  void textEntityDefinitionWithObjectDataSerializesToJson() {
  //    Map<String, Object> objectData = Map.of("name", "John", "age", 30);
  //    messageStubFor(
  //        message()
  //            .withName("Object data stub")
  //            .withBody(equalTo("trigger"))
  //            .willTriggerActions(sendMessage().withBody(objectData).onOriginatingChannel()));
  //
  //    WebsocketTestClient testClient = new WebsocketTestClient();
  //    String url = websocketUrl("/object-data-test");
  //
  //    String response = testClient.sendMessageAndWaitForResponse(url, "trigger");
  //    assertThat(response, jsonEquals("{\"name\":\"John\",\"age\":30}"));
  //  }

  @Test
  void textEntityDefinitionWithDataStoreResolvesFromStore() {
    wireMockServer
        .getOptions()
        .getStores()
        .getObjectStore("testStore")
        .put("testKey", "stored value");

    messageStubFor(
        message()
            .withName("Store data stub")
            .withBody(equalTo("trigger"))
            .willTriggerActions(
                sendMessage().withBodyFromStore("testStore", "testKey").onOriginatingChannel()));

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/store-data-test");

    String response = testClient.sendMessageAndWaitForResponse(url, "trigger");
    assertThat(response, is("stored value"));
  }

  @Test
  void textEntityDefinitionWithDataStoreResolvesObjectFromStoreAsJson() {
    Map<String, Object> storedObject = Map.of("key", "value", "number", 42);
    wireMockServer
        .getOptions()
        .getStores()
        .getObjectStore("objectStore")
        .put("objectKey", storedObject);

    messageStubFor(
        message()
            .withName("Store object data stub")
            .withBody(equalTo("trigger"))
            .willTriggerActions(
                sendMessage()
                    .withBodyFromStore("objectStore", "objectKey")
                    .onOriginatingChannel()));

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/store-object-test");

    String response = testClient.sendMessageAndWaitForResponse(url, "trigger");
    assertThat(response, jsonEquals("{\"number\":42,\"key\":\"value\"}"));
  }

  @Test
  void binaryEqualToCanBeUsedToMatchMessageBody() {
    byte[] expectedBytes = new byte[] {0x01, 0x02, 0x03, 0x04, 0x05};

    messageStubFor(
        message()
            .withName("Binary matching stub")
            .withBody(binaryEqualTo(expectedBytes))
            .willTriggerActions(sendMessage("binary matched!").onOriginatingChannel()));

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/binary-test");

    testClient.connect(url);
    waitAtMost(5, SECONDS).until(testClient::isConnected);

    testClient.sendBinaryMessage(expectedBytes);

    waitAtMost(5, SECONDS).until(() -> testClient.getMessages().contains("binary matched!"));
    assertThat(testClient.getMessages().contains("binary matched!"), is(true));
  }

  @Test
  void binaryMessageCanBeSentAsResponse() {
    byte[] responseBytes = new byte[] {0x0A, 0x0B, 0x0C, 0x0D, 0x0E};

    messageStubFor(
        message()
            .withName("Binary response stub")
            .withBody(equalTo("send-binary"))
            .willTriggerActions(
                sendMessage()
                    .toOriginatingChannel()
                    .withMessage(binaryEntity().setBody(responseBytes))));

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/binary-response-test");

    testClient.connect(url);
    waitAtMost(5, SECONDS).until(testClient::isConnected);

    testClient.sendMessage("send-binary");

    waitAtMost(5, SECONDS).until(() -> !testClient.getBinaryMessages().isEmpty());
    assertThat(testClient.getBinaryMessages().get(0), is(responseBytes));
  }

  @Test
  void binaryMessageFromDataStoreCanBeSentAsResponse() {
    byte[] storedBytes = new byte[] {0x10, 0x20, 0x30, 0x40, 0x50};
    wireMockServer
        .getOptions()
        .getStores()
        .getObjectStore("binaryStore")
        .put("binaryKey", storedBytes);

    messageStubFor(
        message()
            .withName("Binary from store stub")
            .withBody(equalTo("send-stored-binary"))
            .willTriggerActions(
                sendMessage()
                    .toOriginatingChannel()
                    .withMessage(
                        binaryEntity().setDataStore("binaryStore").setDataRef("binaryKey"))));

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/binary-store-test");

    testClient.connect(url);
    waitAtMost(5, SECONDS).until(testClient::isConnected);

    testClient.sendMessage("send-stored-binary");

    waitAtMost(5, SECONDS).until(() -> !testClient.getBinaryMessages().isEmpty());
    assertThat(testClient.getBinaryMessages().get(0), is(storedBytes));
  }

  @Test
  void textEntityDefinitionWithFilePathResolvesFromFilesStore() throws Exception {
    wireMockServer.stop();
    File tempRoot = setupTempFileRoot();
    setupServer(wireMockConfig().withRootDirectory(tempRoot.getAbsolutePath()));

    File messageFile = new File(tempRoot, "__files/message-body.txt");
    writeString(messageFile.toPath(), "Hello from file!");

    messageStubFor(
        message()
            .withName("File body stub")
            .withBody(equalTo("trigger"))
            .willTriggerActions(
                sendMessage().withBodyFromFile("message-body.txt").onOriginatingChannel()));

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/file-body-test");

    String response = testClient.sendMessageAndWaitForResponse(url, "trigger");
    assertThat(response, is("Hello from file!"));
  }

  @Test
  void binaryEntityDefinitionWithFilePathResolvesFromFilesStore() throws Exception {
    File tempRoot = setupServerWithTempFileRoot();

    byte[] binaryContent = new byte[] {0x01, 0x02, 0x03, 0x04, 0x05};
    File binaryFile = new File(tempRoot, "__files/binary-body.bin");
    write(binaryFile.toPath(), binaryContent);

    messageStubFor(
        message()
            .withName("DSL broadcast stub")
            .withBody(equalTo("trigger"))
            .willTriggerActions(
                sendMessage()
                    .toOriginatingChannel()
                    .withMessage(binaryEntity().setFilePath("binary-body.bin"))));

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/binary-file-body-test");

    byte[] response = testClient.sendMessageAndWaitForBinaryResponse(url, "trigger");
    assertThat(response, is(binaryContent));
  }
}
