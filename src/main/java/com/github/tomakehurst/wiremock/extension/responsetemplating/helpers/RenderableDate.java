package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class RenderableDate {

    private final Date date;
    private final String format;
    private final String timezoneName;

    public RenderableDate(Date date, String format, String timezone) {
        this.date = date;
        this.format = format;
        this.timezoneName = timezone;
    }

    @Override
    public String toString() {
        if (format != null) {
            return format.equals("epoch") ?
                String.valueOf(date.getTime()) :
                formatCustom();
        }

        return timezoneName != null ?
            ISO8601Utils.format(date, false, TimeZone.getTimeZone(timezoneName)) :
            ISO8601Utils.format(date, false);
    }

    private String formatCustom() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        if (timezoneName != null) {
            TimeZone zone = TimeZone.getTimeZone(timezoneName);
            dateFormat.setTimeZone(zone);
        }
        return dateFormat.format(date);
    }
}
