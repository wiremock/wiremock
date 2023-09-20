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
package org.wiremock.grpc.client;

import com.example.grpc.GreetingServiceGrpc;
import com.example.grpc.HelloRequest;
import com.example.grpc.HelloResponse;
import com.github.tomakehurst.wiremock.common.Exceptions;
import io.grpc.Channel;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class GreetingsClient {

  private final GreetingServiceGrpc.GreetingServiceBlockingStub stub;
  private final GreetingServiceGrpc.GreetingServiceStub asyncStub;

  public GreetingsClient(Channel channel) {
    stub = GreetingServiceGrpc.newBlockingStub(channel);
    asyncStub = GreetingServiceGrpc.newStub(channel);
  }

  public String greet(String name) {
    return stub.greeting(com.example.grpc.HelloRequest.newBuilder().setName(name).build())
        .getGreeting();
  }

  public List<String> oneGreetingManyReplies(String name) {

    final CountDownLatch latch = new CountDownLatch(1);
    final List<HelloResponse> responses = new ArrayList<>();

    asyncStub.oneGreetingManyReplies(
        HelloRequest.newBuilder().setName(name).build(),
        new StreamObserver<>() {
          @Override
          public void onNext(HelloResponse value) {
            responses.add(value);
          }

          @Override
          public void onError(Throwable t) {}

          @Override
          public void onCompleted() {
            latch.countDown();
          }
        });

    Exceptions.uncheck(() -> latch.await(10, TimeUnit.SECONDS));

    return responses.stream()
        .map(HelloResponse::getGreeting)
        .collect(Collectors.toUnmodifiableList());
  }

  public String manyGreetingsOneReply(String... names) {

    final AtomicReference<HelloResponse> responseHolder = new AtomicReference<>();
    final CountDownLatch latch = new CountDownLatch(1);

    final StreamObserver<HelloRequest> requestObserver =
        asyncStub.manyGreetingsOneReply(
            new StreamObserver<>() {
              @Override
              public void onNext(HelloResponse value) {
                responseHolder.set(value);
              }

              @Override
              public void onError(Throwable t) {}

              @Override
              public void onCompleted() {
                latch.countDown();
              }
            });

    for (String name : names) {
      requestObserver.onNext(HelloRequest.newBuilder().setName(name).build());
    }
    requestObserver.onCompleted();

    Exceptions.uncheck(() -> latch.await(10, TimeUnit.SECONDS));

    return responseHolder.get().getGreeting();
  }
}
