package com.github.tomakehurst.wiremock.common;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SafeNamesTest {

    @Test
    public void generatesNameFromNameWithCharactersSafeForFilenames() {
        String output = SafeNames.makeSafeName("ẄǏŔe mȎČǨs it!");
        assertThat(output, is("wire-mocks-it"));
    }

    @Test
    public void doesNothingWhenAlreadySafe() {
        String input = "wire-mocks__it--123-4";
        String output = SafeNames.makeSafeName(input);
        assertThat(output, is(input));
    }

    @Test
    public void generatesNameFromUrlPathWithCharactersSafeForFilenames() {
        String output = SafeNames.makeSafeNameFromUrl("/hello/1/2/3__!/ẮČĖ--ace/¥$$/$/and/¿?");
        assertThat(output, is("hello_1_2_3___ace--ace___and"));
    }

    @Test
    public void truncatesWhenResultingNameOver200Chars() {
        String output = SafeNames.makeSafeNameFromUrl("/hello/1/2/3__!/ẮČĖ--ace/¥$$/$/andverylongstuffandverylongstuffandverylongstuffandverylongstuffandverylongstuffandverylongstuffandverylongstuffandverylongstuffandverylongstuffandverylongstuffandverylongstuffandverylongstuffandverylongstuff/¿?");
        assertThat(output.length(), is(200));
    }


}
