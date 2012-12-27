package com.github.tomakehurst.wiremock;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ThreadSafeSocketControl implements SocketControl {

    private AtomicReference<DelaySpec> delaySpecRef = new AtomicReference<DelaySpec>(DelaySpec.NONE);

    @Override
    public void setDelay(int requestCount, long milliseconds) {
        delaySpecRef.set(new DelaySpec(requestCount, milliseconds));
    }

    @Override
    public void delayIfRequired() throws InterruptedException {
        DelaySpec delaySpec = delaySpecRef.get();
        if (!delaySpec.requestCountReached()) {
            try {
                System.out.println("Delaying by " + delaySpec.delayMilliseconds);
                Thread.sleep(delaySpec.delayMilliseconds);
                System.out.println("Finished delaying");
            } catch (InterruptedException e) {
                System.out.println("Interrupted");
                // Do nothing - this will happen on connector shutdown
            }
            delaySpec.incrementCountOfRequestsDelayed();
        }
    }

    private static class DelaySpec {

        public final int requestCount;
        public final long delayMilliseconds;
        private AtomicInteger requestsDelayed = new AtomicInteger(0);

        private DelaySpec(int requestCount, long delayMilliseconds) {
            this.requestCount = requestCount;
            this.delayMilliseconds = delayMilliseconds;
        }

        public boolean requestCountReached() {
            return requestsDelayed.get() == requestCount;
        }

        public void incrementCountOfRequestsDelayed() {
            System.out.println("Delayed request count incremented to " + requestsDelayed.incrementAndGet());
        }

        static DelaySpec NONE = new DelaySpec(0, 0);
    }
}
