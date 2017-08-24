package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

/**
 * This class uses HandlebarsXmlHelper as a base an just set a prefix which reduce the written handlebars helper to the
 * relevant part
 */
public class HandlebarsSoapHelper extends HandlebarsXPathHelper {

    @Override
    protected String getXPathPrefix() {
        return "/Envelope/Body/";
    }
}
