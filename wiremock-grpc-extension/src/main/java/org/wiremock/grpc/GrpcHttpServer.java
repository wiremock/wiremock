package org.wiremock.grpc;

import com.github.tomakehurst.wiremock.common.Exceptions;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.jetty11.Jetty11HttpServer;
import com.github.tomakehurst.wiremock.store.BlobStore;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import jakarta.servlet.DispatcherType;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GrpcHttpServer extends Jetty11HttpServer {

    private final StubRequestHandler stubRequestHandler;

    private final List<Descriptors.FileDescriptor> fileDescriptors;


    public GrpcHttpServer(Options options, AdminRequestHandler adminRequestHandler, StubRequestHandler stubRequestHandler, BlobStore protoDescriptorStore) {
        super(options, adminRequestHandler, stubRequestHandler);
        this.stubRequestHandler = stubRequestHandler;
        fileDescriptors = protoDescriptorStore.getAllKeys()
                .filter(key -> key.endsWith(".dsc") || key.endsWith(".desc"))
                .map(key -> protoDescriptorStore.get(key).map(
                                data ->
                                        Exceptions.uncheck(() -> DescriptorProtos.FileDescriptorSet.parseFrom(data), DescriptorProtos.FileDescriptorSet.class)
                        )
                ).filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(fileDescriptorSet -> fileDescriptorSet.getFileList().stream())
                .map(fileDescriptorProto ->
                        Exceptions.uncheck(() -> Descriptors.FileDescriptor.buildFrom(fileDescriptorProto, new Descriptors.FileDescriptor[0]), Descriptors.FileDescriptor.class)
                )
                .toList();
    }

    @Override
    protected void decorateMockServiceContextBeforeConfig(ServletContextHandler mockServiceContext) {
        final GrpcFilter grpcFilter = new GrpcFilter(stubRequestHandler, fileDescriptors);
        final FilterHolder filterHolder = new FilterHolder(grpcFilter);
        mockServiceContext.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));
    }
}
