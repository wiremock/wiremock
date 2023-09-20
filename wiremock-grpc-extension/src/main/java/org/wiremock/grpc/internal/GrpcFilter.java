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
package org.wiremock.grpc.internal;

import static java.util.concurrent.TimeUnit.SECONDS;

import com.github.tomakehurst.wiremock.common.Exceptions;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import io.grpc.*;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.servlet.jakarta.GrpcServlet;
import io.grpc.servlet.jakarta.ServletAdapter;
import io.grpc.stub.ServerCalls;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class GrpcFilter extends HttpFilter {

  private final GrpcServlet grpcServlet;
  private final StubRequestHandler stubRequestHandler;
  private final List<Descriptors.FileDescriptor> fileDescriptors;

  public GrpcFilter(
      StubRequestHandler stubRequestHandler, List<Descriptors.FileDescriptor> fileDescriptors) {
    this.stubRequestHandler = stubRequestHandler;
    this.fileDescriptors = fileDescriptors;
    grpcServlet = new GrpcServlet(buildServices());
  }

  private List<BindableService> buildServices() {
    return fileDescriptors.stream()
        .flatMap(fileDescriptor -> fileDescriptor.getServices().stream())
        .map(
            serviceDescriptor ->
                (BindableService)
                    () -> {
                      final ServerServiceDefinition.Builder builder =
                          ServerServiceDefinition.builder(serviceDescriptor.getFullName());
                      serviceDescriptor
                          .getMethods()
                          .forEach(
                              methodDescriptor ->
                                  builder.addMethod(
                                      buildMessageDescriptorInstance(
                                          serviceDescriptor, methodDescriptor),
                                      buildHandler(serviceDescriptor, methodDescriptor)));
                      return builder.build();
                    })
        .collect(Collectors.toUnmodifiableList());
  }

  private ServerCallHandler<DynamicMessage, DynamicMessage> buildHandler(
      Descriptors.ServiceDescriptor serviceDescriptor,
      Descriptors.MethodDescriptor methodDescriptor) {
    return methodDescriptor.isClientStreaming()
        ? ServerCalls.asyncClientStreamingCall(
            new ClientStreamingServerCallHandler(
                stubRequestHandler, serviceDescriptor, methodDescriptor))
        : ServerCalls.asyncUnaryCall(
            new UnaryServerCallHandler(stubRequestHandler, serviceDescriptor, methodDescriptor));
  }

  private static MethodDescriptor<DynamicMessage, DynamicMessage> buildMessageDescriptorInstance(
      Descriptors.ServiceDescriptor serviceDescriptor,
      Descriptors.MethodDescriptor methodDescriptor) {
    return MethodDescriptor.<DynamicMessage, DynamicMessage>newBuilder()
        .setType(getMethodTypeFromDesc(methodDescriptor))
        .setFullMethodName(
            MethodDescriptor.generateFullMethodName(
                serviceDescriptor.getFullName(), methodDescriptor.getName()))
        .setRequestMarshaller(
            ProtoUtils.marshaller(
                DynamicMessage.getDefaultInstance(methodDescriptor.getInputType())))
        .setResponseMarshaller(
            ProtoUtils.marshaller(
                DynamicMessage.getDefaultInstance(methodDescriptor.getOutputType())))
        .build();
  }

  private static MethodDescriptor.MethodType getMethodTypeFromDesc(
      Descriptors.MethodDescriptor methodDesc) {
    if (!methodDesc.isServerStreaming() && !methodDesc.isClientStreaming()) {
      return MethodDescriptor.MethodType.UNARY;
    } else if (methodDesc.isServerStreaming() && !methodDesc.isClientStreaming()) {
      return MethodDescriptor.MethodType.SERVER_STREAMING;
    } else if (!methodDesc.isServerStreaming()) {
      return MethodDescriptor.MethodType.CLIENT_STREAMING;
    } else {
      return MethodDescriptor.MethodType.BIDI_STREAMING;
    }
  }

  @Override
  protected void doFilter(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (!ServletAdapter.isGrpc(request)) {
      chain.doFilter(request, response);
      return;
    }

    ServerAddress.set(request.getScheme(), request.getLocalAddr(), request.getLocalPort());
    grpcServlet.service(request, response);
  }

  public static class ServerAddress {
    private static final CompletableFuture<ServerAddress> instance = new CompletableFuture<>();

    public static void set(String scheme, String hostname, int port) {
      instance.complete(new ServerAddress(scheme, hostname, port));
    }

    public static ServerAddress get() {
      return Exceptions.uncheck(() -> instance.get(5, SECONDS), ServerAddress.class);
    }

    final String scheme;
    final String hostname;
    final int port;

    public ServerAddress(String scheme, String hostname, int port) {
      this.scheme = scheme;
      this.hostname = hostname;
      this.port = port;
    }
  }
}
