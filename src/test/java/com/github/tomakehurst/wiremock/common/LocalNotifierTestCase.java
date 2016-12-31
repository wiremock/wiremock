package com.github.tomakehurst.wiremock.common;

import com.google.common.base.Throwables;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

/**
 * A rather superfluous test case for thread local notifiers held by {@link LocalNotifier}
 */
public class LocalNotifierTestCase {
    private static final int THREADS = 16;// number of threads to spawn for concurrent testig
    private static final int EXECUTOR_TIMEOUT = 60000; //[ms]

    @Test
    public void testThreadLocalDefaultInstance() throws Exception {
        runConcurrently(THREADS, EXECUTOR_TIMEOUT, new Runnable() {
            final Set<Notifier> uniqueNotifiers = createConcurrentIdentitySet();

            @Override
            public void run() {
                Notifier defaultNotifier1 = LocalNotifier.notifier();
                Notifier defaultNotifier2 = LocalNotifier.notifier();

                assertTrue(uniqueNotifiers.add(defaultNotifier1));

                assertNotNull(defaultNotifier1);
                assertSame(defaultNotifier1, defaultNotifier2);
            }
        });
    }

    @Test
    public void testThreadLocalCustomInstance() throws Exception {
        runConcurrently(THREADS, EXECUTOR_TIMEOUT, new Runnable() {
            final Set<Notifier> uniqueNotifiers = createConcurrentIdentitySet();

            @Override
            public void run() {
                final Notifier defaultNotifier = LocalNotifier.notifier();
                assertTrue(uniqueNotifiers.add(defaultNotifier));

                Notifier customNotifier = new Notifier() {
                    @Override
                    public void info(String message) {
                    }

                    @Override
                    public void error(String message) {
                    }

                    @Override
                    public void error(String message, Throwable t) {
                    }
                };

                LocalNotifier.set(customNotifier);

                Notifier notifier1 = LocalNotifier.notifier();
                Notifier notifier2 = LocalNotifier.notifier();

                assertSame(customNotifier, notifier1);
                assertSame(notifier1, notifier2);

                assertTrue(uniqueNotifiers.add(notifier1));
            }
        });
    }

    @Test
    public void testThreadLocalNullReplacement() throws Exception {
        runConcurrently(THREADS, EXECUTOR_TIMEOUT, new Runnable() {
            final Set<Notifier> uniqueNotifiers = createConcurrentIdentitySet();

            @Override
            public void run() {
                final Notifier defaultNotifier = LocalNotifier.notifier();
                assertTrue(uniqueNotifiers.add(defaultNotifier));

                LocalNotifier.set(null);

                Notifier notifier1 = LocalNotifier.notifier();
                assertNotNull(notifier1);
                assertNotSame(notifier1, defaultNotifier);

                Notifier notifier2 = LocalNotifier.notifier();
                assertSame(notifier1, notifier2);

                assertTrue(uniqueNotifiers.add(notifier1));
            }
        });
    }

    private static <T> Set<T> createConcurrentIdentitySet() {
        return Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<T, Boolean>()));
    }

    /**
     * Executes a single test runnable concurrently in an individual thread per execution.
     * The calling thread is blocked until all test threads have terminated or an unexpected error occurs.
     *
     * @param runnable test implementation
     */
    private static void runConcurrently(final int threadCount, final int timeout, final Runnable runnable) throws Exception {
        if (threadCount < 1)
            throw new IllegalAccessException("threadCount must be positive");
        if (runnable == null)
            throw new NullPointerException();

        final BlockingQueue<Throwable> errors = new LinkedBlockingQueue<>();
        final List<Thread> threads = new ArrayList<>(threadCount);

        //prepare threads
        for (int i = 0; i < threadCount; ++i)
            threads.add(new Thread(runnable, "junit:" + LocalNotifierTestCase.class.getSimpleName()) {
                @Override
                public void run() {
                    try {
                        super.run();
                    } catch (Throwable throwable) {
                        errors.add(throwable);
                    }
                }
            });

        try {
            //start all threads
            for (Thread thread : threads)
                thread.start();

            //join all threads with a timeout
            final long joinStartTime = System.currentTimeMillis();

            for (Iterator<Thread> iterator = threads.iterator(); iterator.hasNext(); ) {
                Thread thread = iterator.next();
                boolean alive = thread.isAlive();
                if (alive) {
                    if (timeout >= 0) {
                        long remainingDelay = (joinStartTime + timeout) - System.currentTimeMillis();
                        if (remainingDelay > 0) {
                            thread.join(remainingDelay, 0);
                            alive = thread.isAlive();
                        }
                        if (alive)
                            throw new TimeoutException("execution timed out");
                    } else {
                        thread.join();
                        alive = thread.isAlive();
                        if (alive)
                            throw new Error("joined thread appears to be alive");
                    }
                }

                iterator.remove();
            }

            //wrap exceptions from threads into an appropriate exception
            if (!errors.isEmpty()) {
                int assertionErrorCount = 0;
                int errorCount;

                for (Throwable throwable : errors)
                    if (throwable instanceof AssertionError)
                        ++assertionErrorCount;
                errorCount = errors.size() - assertionErrorCount;

                String message = "Concurrent execution failed: " + assertionErrorCount + " assertion errors, " + errorCount + " other errors";
                Throwable error = assertionErrorCount == errors.size() ? new AssertionError(message) : new RuntimeException(message);
                for (Throwable suppressed : errors)
                    error.addSuppressed(suppressed);
                throw Throwables.propagate(error);
            }
        } finally {
            for (Thread thread : threads)
                thread.interrupt();
        }
    }
}
