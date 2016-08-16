package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.github.tomakehurst.wiremock.http.QueryParameter;

import java.text.ParseException;
import java.util.Date;

public class Conversions {

    public static Integer toInt(QueryParameter parameter) {
        return parameter.isPresent() ?
            Integer.valueOf(parameter.firstValue()) :
            null;
    }

    public static Date toDate(QueryParameter parameter) {
        try {
            return parameter.isPresent() ?
                new ISO8601DateFormat().parse(parameter.firstValue()) :
                null;
        } catch (ParseException e) {
            throw new IllegalArgumentException(parameter.firstValue() + " is not a valid ISO8601 date");
        }
    }
}
