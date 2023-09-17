package org.wiremock.grpc;

import com.github.tomakehurst.wiremock.common.Exceptions;
import com.github.tomakehurst.wiremock.common.JdkBase64Encoder;
import com.github.tomakehurst.wiremock.common.Strings;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.util.JsonFormat;
import io.grpc.BindableService;
import io.grpc.MethodDescriptor;
import io.grpc.ServerServiceDefinition;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.TimeUnit.SECONDS;

public class GrpcFilter extends HttpFilter {

    private final GrpcServlet grpcServlet;
    private final StubRequestHandler stubRequestHandler;
    private final List<Descriptors.FileDescriptor> fileDescriptors;

    public GrpcFilter(StubRequestHandler stubRequestHandler, List<Descriptors.FileDescriptor> fileDescriptors) {
        this.stubRequestHandler = stubRequestHandler;
        this.fileDescriptors = fileDescriptors;
        grpcServlet = new GrpcServlet(buildServices());
    }

    private List<BindableService> buildServices() {
        return fileDescriptors.stream()
                .flatMap(fileDescriptor -> fileDescriptor.getServices().stream())
                .map(serviceDescriptor -> (BindableService) () -> {
                    final ServerServiceDefinition.Builder builder = ServerServiceDefinition.builder(serviceDescriptor.getFullName());
                    serviceDescriptor.getMethods().forEach(methodDescriptor -> {

                        final MethodDescriptor<DynamicMessage, DynamicMessage> methodDescriptorInstance = MethodDescriptor.<DynamicMessage, DynamicMessage>newBuilder()
                                .setType(getMethodTypeFromDesc(methodDescriptor))
                                .setFullMethodName(MethodDescriptor.generateFullMethodName(serviceDescriptor.getFullName(), methodDescriptor.getName()))
                                .setRequestMarshaller(ProtoUtils.marshaller(
                                        DynamicMessage.getDefaultInstance(methodDescriptor.getInputType())))
                                .setResponseMarshaller(ProtoUtils.marshaller(
                                        DynamicMessage.getDefaultInstance(methodDescriptor.getOutputType())))
                                .build();

                        builder.addMethod(methodDescriptorInstance, ServerCalls.asyncUnaryCall((request, responseObserver) -> {
                                final ServerAddress serverAddress = ServerAddress.get();

                                final GrpcRequest wireMockRequest = new GrpcRequest(
                                    serverAddress.scheme,
                                    serverAddress.hostname,
                                    serverAddress.port,
                                    serviceDescriptor.getFullName(),
                                    methodDescriptor.getName(),
                                    request);

                                stubRequestHandler.handle(wireMockRequest, (req, resp, attributes) -> {
                                    DynamicMessage.Builder messageBuilder = DynamicMessage.newBuilder(methodDescriptor.getOutputType());
                                    Exceptions.uncheck(() -> JsonFormat.parser().merge(resp.getBodyAsString(), messageBuilder));
                                    responseObserver.onNext(messageBuilder.build());
                                    responseObserver.onCompleted();
                                }, ServeEvent.of(wireMockRequest));
                            }));
                    });
                    return builder.build();
                })
                .toList();
    }

    private static MethodDescriptor.MethodType getMethodTypeFromDesc(Descriptors.MethodDescriptor methodDesc) {
        if (!methodDesc.isServerStreaming()
                && !methodDesc.isClientStreaming()) {
            return MethodDescriptor.MethodType.UNARY;
        } else if (methodDesc.isServerStreaming()
                && !methodDesc.isClientStreaming()) {
            return MethodDescriptor.MethodType.SERVER_STREAMING;
        } else if (!methodDesc.isServerStreaming()) {
            return MethodDescriptor.MethodType.CLIENT_STREAMING;
        } else {
            return MethodDescriptor.MethodType.BIDI_STREAMING;
        }
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
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

        public static String getBaseUrl() {
            final ServerAddress serverAddress = get();
            return serverAddress.scheme + "://" + serverAddress.hostname + ":" + serverAddress.port;
        }

        private final String scheme;
        private final String hostname;
        private final int port;

        public ServerAddress(String scheme, String hostname, int port) {
            this.scheme = scheme;
            this.hostname = hostname;
            this.port = port;
        }
    }
}
