package com.github.tomakehurst.wiremock.verification.diff;

import java.util.List;

public class DiffSection {

    private final String name;
    private final List<DiffLine<?>> lines;

    public DiffSection(String name, List<DiffLine<?>> lines) {
        this.name = name;
        this.lines = lines;
    }

    public String getName() {
        return name;
    }

    public List<DiffLine<?>> getLines() {
        return lines;
    }
}
