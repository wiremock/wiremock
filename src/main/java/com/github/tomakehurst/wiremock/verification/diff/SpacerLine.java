package com.github.tomakehurst.wiremock.verification.diff;

public class SpacerLine extends DiffLine<Void> {

    public static SpacerLine SPACER = new SpacerLine();

    public SpacerLine() {
        super("spacer", null, null, "");
    }

    @Override
    public Object getActual() {
        return "";
    }

    @Override
    public String getMessage() {
        return null;
    }

    @Override
    protected boolean isExactMatch() {
        return true;
    }
}
