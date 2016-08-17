package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public class SingleItemResult<T> {

    private final T item;

    @JsonCreator
    public SingleItemResult(T item) {
        this.item = item;
    }

    @JsonValue
    public T getItem() {
        return item;
    }

    @JsonIgnore
    public boolean isPresent() {
        return item != null;
    }
}
