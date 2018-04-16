package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Options;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;
import java.util.UUID;

public class HandlebarsRandomValuesHelper extends HandlebarsHelper<Void> {

    @Override
    public Object apply(Void context, Options options) throws IOException {
        int length = options.hash("length", 36);
        boolean uppercase = options.hash("uppercase", false);

        String type = options.hash("type", "ALPHANUMERIC");
        String rawValue;

        switch (type) {
            case "ALPHANUMERIC":
                rawValue = RandomStringUtils.randomAlphanumeric(length);
                break;
            case "ALPHABETIC":
                rawValue = RandomStringUtils.randomAlphabetic(length);
                break;
            case "NUMERIC":
                rawValue = RandomStringUtils.randomNumeric(length);
                break;
            case "ALPHANUMERIC_AND_SYMBOLS":
                rawValue = RandomStringUtils.random(length);
                break;
            case "UUID":
                rawValue = UUID.randomUUID().toString();
                break;
            default:
                rawValue = RandomStringUtils.randomAscii(length);
                break;

        }
        return uppercase?
            rawValue.toUpperCase() :
            rawValue.toLowerCase();
    }
}
