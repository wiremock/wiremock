package com.github.tomakehurst.wiremock.verification.diff;

import com.github.tomakehurst.wiremock.matching.EqualToPattern;

public class SectionDelimiter extends DiffLine<String> {

    public SectionDelimiter(String title) {
        super(title, new EqualToPattern(title), title, title);
    }

    public SectionDelimiter(String left, String right) {
        super(left, new EqualToPattern(left), right, left);
    }

    @Override
    protected boolean isExactMatch() {
        return true;
    }

}
