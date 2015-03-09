package com.github.tomakehurst.wiremock.common;

import com.google.common.base.Optional;

/**
 * Exposed Jetty tuning options. See: http://download.eclipse.org/jetty/stable-7/apidocs/org/eclipse/jetty/server/AbstractConnector.html
 */
public class JettySettings {
    private final Optional<Integer> acceptors;
    private final Optional<Integer> acceptQueueSize;
    private final Optional<Integer> requestHeaderSize;

    private JettySettings(Optional<Integer> acceptors,
                          Optional<Integer> acceptQueueSize,
                          Optional<Integer> requestHeaderSize) {
        this.acceptors = acceptors;
        this.acceptQueueSize = acceptQueueSize;
        this.requestHeaderSize = requestHeaderSize;
    }

    public Optional<Integer> getAcceptors() {
        return acceptors;
    }

    public Optional<Integer> getAcceptQueueSize() {
        return acceptQueueSize;
    }

    public Optional<Integer> getRequestHeaderSize() {
        return requestHeaderSize;
    }

    @Override
    public String toString() {
        return "JettySettings{" +
                "acceptors=" + acceptors +
                ", acceptQueueSize=" + acceptQueueSize +
                ", requestHeaderSize=" + requestHeaderSize +
                '}';
    }

    public static class Builder {
        private Integer acceptors;
        private Integer acceptQueueSize;
        private Integer requestHeaderSize;

        private Builder() {
        }

        public static Builder aJettySettings() {
            return new Builder();
        }

        public Builder withAcceptors(Integer acceptors) {
            this.acceptors = acceptors;
            return this;
        }

        public Builder withAcceptQueueSize(Integer acceptQueueSize) {
            this.acceptQueueSize = acceptQueueSize;
            return this;
        }

        public Builder withRequestHeaderSize(Integer requestHeaderSize) {
            this.requestHeaderSize = requestHeaderSize;
            return this;
        }

        public JettySettings build() {
            return new JettySettings(Optional.fromNullable(acceptors),
                    Optional.fromNullable(acceptQueueSize),
                    Optional.fromNullable(requestHeaderSize));
        }
    }


}
