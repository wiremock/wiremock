package com.github.tomakehurst.wiremock.common;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.base.Function;
import com.google.common.base.Joiner;

import java.util.List;
import java.util.regex.PatternSyntaxException;

import static com.google.common.collect.Lists.transform;

public class JsonException extends InvalidInputException {

    protected JsonException(Errors errors) {
        super(errors);
    }

    public static JsonException fromJackson(JsonMappingException e) {
        Throwable rootCause = getRootCause(e);

        String message = rootCause.getMessage();
        if (rootCause instanceof PatternSyntaxException) {
            PatternSyntaxException patternSyntaxException = (PatternSyntaxException) rootCause;
            message = patternSyntaxException.getMessage();
        } else if (rootCause instanceof JsonMappingException) {
            message = ((JsonMappingException) rootCause).getOriginalMessage();
        }

        List<String> nodes = transform(e.getPath(), TO_NODE_NAMES);
        String pointer = '/' + Joiner.on('/').join(nodes);

        return new JsonException(Errors.single(10, pointer, "Error parsing JSON", message));
    }

    private static Throwable getRootCause(Throwable e) {
        if (e.getCause() != null) {
            return getRootCause(e.getCause());
        }

        return e;
    }

    private static final Function<JsonMappingException.Reference, String> TO_NODE_NAMES = new Function<JsonMappingException.Reference, String>() {
        @Override
        public String apply(JsonMappingException.Reference input) {
            if (input.getFieldName() != null) {
                return input.getFieldName();
            }

            return String.valueOf(input.getIndex());
        }
    };

}
