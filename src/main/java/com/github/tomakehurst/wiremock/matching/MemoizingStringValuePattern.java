package com.github.tomakehurst.wiremock.matching;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

public abstract class MemoizingStringValuePattern extends StringValuePattern {

    private final LoadingCache<String, MatchResult> cache = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, MatchResult>() {
                @Override
                public MatchResult load(String value) {
                    return new MemoizingMatchResult(calculateMatch(value));
                }
            });

    public MemoizingStringValuePattern(String expectedValue) {
        super(expectedValue);
    }

    @Override
    public final MatchResult match(String value) {
        if (value == null) {
            return MatchResult.noMatch();
        }

        try {
            return cache.get(value);
        } catch (ExecutionException e) {
            return throwUnchecked(e, MatchResult.class);
        }
    }

    protected abstract MatchResult calculateMatch(String value);
}
