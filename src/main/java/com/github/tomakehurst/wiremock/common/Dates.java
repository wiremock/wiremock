package com.github.tomakehurst.wiremock.common;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.databind.util.ISO8601Utils;

import java.text.ParseException;
import java.util.Date;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

public class Dates {

    public static Date parse(String dateString) {
        try {
            return new ISO8601DateFormat().parse(dateString);
        } catch (ParseException e) {
            return throwUnchecked(e, Date.class);
        }
    }

    public static String format(Date date) {
        return ISO8601Utils.format(date);
    }
}
