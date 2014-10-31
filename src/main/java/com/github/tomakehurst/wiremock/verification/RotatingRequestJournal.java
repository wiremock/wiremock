package com.github.tomakehurst.wiremock.verification;

/**
 * Request journal that has a maximum number of entries it holds. If a new entry is added and the size of the journal
 * becomes larger than the maximum number of entries the oldest entry must be discarded.
 */
public interface RotatingRequestJournal extends RequestJournal {
    /**
     * @param maxEntries {@link #getMaxEntries()}
     */
    void setMaxEntries(Integer maxEntries);

    /**
     * @return New maximum number of entries. May be null to disable discarding of old entries.
     */
    Integer getMaxEntries();
}
