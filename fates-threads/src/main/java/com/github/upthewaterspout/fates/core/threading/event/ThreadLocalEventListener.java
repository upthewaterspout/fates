/*
 * Copyright 2018 Dan Smith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.upthewaterspout.fates.core.threading.event;

import static java.lang.Boolean.FALSE;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * A decorator for a {@link ExecutionEventListener} that is only enabled for the current thread
 * and any thread spawned by the current thread.
 *
 * This listener is used to make sure that only threads under test are controlled by the
 * scheduler.
 */
public class ThreadLocalEventListener extends DelegatingExecutionEventListener {
  private Set<Thread> enabledThreads = new CopyOnWriteArraySet<>();
  private ThreadLocal<Boolean> currentThreadEnabled =
      ThreadLocal.withInitial(() -> enabledThreads.contains(Thread.currentThread()));


  public ThreadLocalEventListener(ExecutionEventListener delegate) {
    super(delegate);
    enabledThreads.add(Thread.currentThread());
    currentThreadEnabled.set(Boolean.TRUE);
  }

  @Override
  protected boolean beforeEvent() {
    return enabled();
  }

  public boolean enabled() {
    return currentThreadEnabled.get();
  }

  @Override
  public void beforeThreadStart(Thread thread) {
    if(beforeEvent()) {
      //Make the rest of thread creation atomic until after the thread start
      currentThreadEnabled.set(FALSE);

      //Mark the new thread as enabled
      enabledThreads.add(thread);
      delegate.beforeThreadStart(thread);
    }
  }

  @Override
  public void afterThreadStart(Thread thread) {
    currentThreadEnabled.set(enabledThreads.contains(Thread.currentThread()));
    if(enabled()) {
      delegate.afterThreadStart(thread);
    }
  }

  @Override
  public void beforeThreadExit() {
    beforeThreadExit(Thread.currentThread());
  }

  public void beforeThreadExit(Thread thread) {
    if(enabled()) {
      currentThreadEnabled.set(FALSE);
      enabledThreads.remove(thread);
      delegate.beforeThreadExit();
    }
  }

  @Override
  public void postValidation() {
    if(!enabledThreads.equals(Collections.singleton(Thread.currentThread()))) {
      Set<Thread> remainingThreads = new HashSet<>(enabledThreads);
      remainingThreads.remove(Thread.currentThread());
      waitForRemainingThreads(remainingThreads);
    }
  }

  private void waitForRemainingThreads(Set<Thread> remainingThreads) {
    for(Thread thread : remainingThreads) {
      try {
        replaceJoin(null, thread, 0, 0);
      } catch (InterruptedException e) {
        //ignore
      }
    }
  }
}
