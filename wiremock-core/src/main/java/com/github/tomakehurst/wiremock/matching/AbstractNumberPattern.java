package com.github.tomakehurst.wiremock.matching;

public abstract class AbstractNumberPattern extends StringValuePattern {

    protected final Number expectedNumber;

    public AbstractNumberPattern(Number expectedNumber) {
        super(expectedNumber.toString());
        this.expectedNumber = expectedNumber;
    }
}
