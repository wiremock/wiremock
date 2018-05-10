package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.github.jknack.handlebars.Options;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ParseDateHelperTest {

    private static final DateFormat df = new ISO8601DateFormat();
    private static final DateFormat localDf = new SimpleDateFormat("yyyy-MM-dd");

    private ParseDateHelper helper;

    @Before
    public void init() {
        helper = new ParseDateHelper();
    }

    @Test
    public void parsesAnISO8601DateWhenNoFormatSpecified() throws Exception {
        ImmutableMap<String, Object> optionsHash = ImmutableMap.of();

        String inputDate = "2018-05-01T01:02:03Z";
        Object output = render(inputDate, optionsHash);

        Date expectedDate = df.parse(inputDate);
        assertThat(output, instanceOf(Date.class));
        assertThat(((Date) output), is((expectedDate)));
    }

    @Test
    public void parsesDateWithSuppliedFormat() throws Exception {
        ImmutableMap<String, Object> optionsHash = ImmutableMap.<String, Object>of(
            "format", "dd/MM/yyyy"
        );

        String inputDate = "01/02/2003";
        Object output = render(inputDate, optionsHash);

        Date expectedDate = localDf.parse("2003-02-01");
        assertThat(output, instanceOf(Date.class));
        assertThat(((Date) output), is((expectedDate)));
    }

    private Object render(String context, ImmutableMap<String, Object> optionsHash) throws IOException {
        return helper.apply(context,
            new Options.Builder(null, null, null, null, null)
                .setHash(optionsHash).build()
        );
    }
}
