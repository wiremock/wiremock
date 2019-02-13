package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.List;

public class StubImport {

    public static class Options {

        public enum DuplicatePolicy { OVERWRITE, IGNORE }

        private final DuplicatePolicy duplicatePolicy;
        private final Boolean deleteAllNotInImport;

        public Options(@JsonProperty("duplicatePolicy") DuplicatePolicy duplicatePolicy,
                       @JsonProperty("deleteAllNotInImport") Boolean deleteAllNotInImport) {
            this.duplicatePolicy = duplicatePolicy;
            this.deleteAllNotInImport = deleteAllNotInImport;
        }

        public DuplicatePolicy getDuplicatePolicy() {
            return duplicatePolicy;
        }

        public Boolean getDeleteAllNotInImport() {
            return deleteAllNotInImport;
        }

        public static final Options DEFAULTS = new Options(DuplicatePolicy.OVERWRITE, false);
    }

    private final List<StubMapping> mappings;
    private final Options importOptions;

    public StubImport(@JsonProperty("mappings") List<StubMapping> mappings,
                      @JsonProperty("importOptions") Options importOptions) {
        this.mappings = mappings;
        this.importOptions = importOptions;
    }

    public List<StubMapping> getMappings() {
        return mappings;
    }

    public Options getImportOptions() {
        return importOptions;
    }
}
