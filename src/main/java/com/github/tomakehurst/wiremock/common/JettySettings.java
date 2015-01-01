package com.github.tomakehurst.wiremock.common;

import com.google.common.base.Optional;

/**
 * Exposed Jetty tuning options. See: http://download.eclipse.org/jetty/stable-7/apidocs/org/eclipse/jetty/server/AbstractConnector.html
 */
public class JettySettings {
    private final Optional<Integer> acceptors;
    private final Optional<Integer> acceptQueueSize;

    private JettySettings(Optional<Integer> acceptors, Optional<Integer> acceptQueueSize) {
        this.acceptors = acceptors;
        this.acceptQueueSize = acceptQueueSize;
    }

    public Optional<Integer> getAcceptors() {
        return acceptors;
    }

    public Optional<Integer> getAcceptQueueSize() {
        return acceptQueueSize;
    }

    @Override
    public String toString() {
        return "JettySettings{" +
                "acceptors=" + acceptors +
                ", acceptQueueSize=" + acceptQueueSize +
                '}';
    }

    public static class Builder {
        private Integer acceptors;
        private Integer acceptQueueSize;

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

        public JettySettings build() {
            JettySettings jettySettings = new JettySettings(Optional.fromNullable(acceptors), Optional.fromNullable(acceptQueueSize));
            return jettySettings;
        }
    }


}
