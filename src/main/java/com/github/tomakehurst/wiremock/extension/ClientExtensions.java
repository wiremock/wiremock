package com.github.tomakehurst.wiremock.extension;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public class ClientExtensions extends Extensions {

    public ClientExtensions(ExtensionDeclarations extensionDeclarations) {
        super(extensionDeclarations);
    }

    public void load() {
        Stream.concat(
                        extensionDeclarations.getClassNames().stream().map(Extensions::loadClass),
                        extensionDeclarations.getClasses().stream())
                .map(ServerExtensions::load)
                .forEach(
                        extension -> {
                            if (loadedExtensions.containsKey(extension.getName())) {
                                throw new IllegalArgumentException(
                                        "Duplicate extension name: " + extension.getName());
                            }
                            loadedExtensions.put(extension.getName(), extension);
                        });

        loadedExtensions.putAll(extensionDeclarations.getInstances());
        final Stream<ExtensionFactory> allFactories = extensionDeclarations.getFactories().stream();

        loadedExtensions.putAll(
                allFactories
                        .map(ExtensionFactory::createForClient)
                        .flatMap(List::stream)
                        .collect(toMap(Extension::getName, Function.identity())));
    }
}
