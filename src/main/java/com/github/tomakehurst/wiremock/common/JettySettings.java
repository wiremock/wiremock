package com.github.tomakehurst.wiremock.common;

/**
 * Exposed Jetty tuning options. See: http://download.eclipse.org/jetty/stable-7/apidocs/org/eclipse/jetty/server/AbstractConnector.html
 */
public class JettySettings {
    private final int acceptors;
    private final int acceptQueueSize;

    public JettySettings(int acceptors, int acceptQueueSize) {
        this.acceptors = acceptors;
        this.acceptQueueSize = acceptQueueSize;
    }

    public int getAcceptors() {
        return acceptors;
    }

    public int getAcceptQueueSize() {
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
        private int acceptors;
        private int acceptQueueSize;

        private Builder() {
        }

        public static Builder aJettySettings() {
            return new Builder();
        }

        public Builder withAcceptors(int acceptors) {
            this.acceptors = acceptors;
            return this;
        }

        public Builder withAcceptQueueSize(int acceptQueueSize) {
            this.acceptQueueSize = acceptQueueSize;
            return this;
        }

        public JettySettings build() {
            JettySettings jettySettings = new JettySettings(acceptors, acceptQueueSize);
            return jettySettings;
        }
    }


}
