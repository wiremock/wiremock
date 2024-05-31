package com.github.tomakehurst.wiremock.common;

import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;
import com.github.tomakehurst.wiremock.matching.ContentPattern;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class ExtensibleJsonSubtypeResolver extends StdSubtypeResolver {

    private final List<Class<?>> contentPatternSubtypes;

    public ExtensibleJsonSubtypeResolver(List<Class<?>> contentPatternSubtypes) {
        this.contentPatternSubtypes = contentPatternSubtypes;
    }

    @Override
    protected Collection<NamedType> _combineNamedAndUnnamed(Class<?> rawBase, Set<Class<?>> typesHandled, Map<String, NamedType> byName) {
        final Collection<NamedType> namedTypes = super._combineNamedAndUnnamed(rawBase, typesHandled, byName);
        if (ContentPattern.class.isAssignableFrom(rawBase)) {
            namedTypes.addAll(this.contentPatternSubtypes.stream()
                    .map(NamedType::new)
                    .collect(toList()));
        }
        return namedTypes;
    }
}
