package com.github.tomakehurst.wiremock.matching;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public class MemoizingMatchResult extends MatchResult {

    private final Supplier<Double> memoizedDistance = Suppliers.memoize(new Supplier<Double>() {
        @Override
        public Double get() {
            return target.getDistance();
        }
    });

    private final Supplier<Boolean> memoizedExactMatch = Suppliers.memoize(new Supplier<Boolean>() {
        @Override
        public Boolean get() {
            return target.isExactMatch();
        }
    });

    private final MatchResult target;

    public MemoizingMatchResult(MatchResult target) {
        this.target = target;
    }

    @Override
    public boolean isExactMatch() {
        return memoizedExactMatch.get();
    }

    @Override
    public double getDistance() {
        return memoizedDistance.get();
    }
}
