/*
 * Copyright (C) 2024-2025 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.common;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class KeyLocks {

  private final ConcurrentHashMap<String, LockWrapper> locks = new ConcurrentHashMap<>();

  @SuppressWarnings("unchecked")
  public <T> T withLock(String key, Callable<T> action) {
    try {
      lock(key);
      return (T) Exceptions.uncheck(action::call, Object.class);
    } finally {
      unlock(key);
    }
  }

  private void lock(String key) {
    LockWrapper lockWrapper =
        locks.compute(key, (k, v) -> v == null ? new LockWrapper() : v.addThreadInQueue());
    lockWrapper.lock.lock();
  }

  private void unlock(String key) {
    LockWrapper lockWrapper = locks.get(key);
    lockWrapper.lock.unlock();
    if (lockWrapper.removeThreadFromQueue() == 0) {
      locks.remove(key, lockWrapper);
    }
  }

  private static class LockWrapper {
    private final Lock lock = new ReentrantLock();
    private final AtomicInteger numberOfThreadsInQueue = new AtomicInteger(1);

    private LockWrapper addThreadInQueue() {
      numberOfThreadsInQueue.incrementAndGet();
      return this;
    }

    private int removeThreadFromQueue() {
      return numberOfThreadsInQueue.decrementAndGet();
    }
  }
}
