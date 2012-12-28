package com.github.tomakehurst.wiremock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;

public class ThreadSafeRequestDelayControl implements RequestDelayControl {

    private AtomicInteger delayMilliseconds = new AtomicInteger(0);
    private ConcurrentHashMap<Thread, Object> threadsBeingDelayed = new ConcurrentHashMap<Thread, Object>();

    @Override
    public void setDelay(int milliseconds) {
        notifier().info("Setting request delay to " + milliseconds + "ms");
        delayMilliseconds.set(milliseconds);
    }

    @Override
    public void clearDelay() {
        notifier().info("Clearing request delay");
        delayMilliseconds.set(0);
        cancelAllDelays();
    }

    @Override
    public void delayIfRequired() throws InterruptedException {
        int millis = delayMilliseconds.get();
        if (millis != 0) {
            threadsBeingDelayed.put(Thread.currentThread(), new Object());
            Thread.sleep(millis);
        }
    }

    public void cancelAllDelays() {
        for (Thread thread: threadsBeingDelayed.keySet()) {
            thread.interrupt();
        }
    }
}
