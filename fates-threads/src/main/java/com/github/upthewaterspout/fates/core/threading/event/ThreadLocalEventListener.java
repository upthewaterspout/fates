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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.github.upthewaterspout.fates.core.threading.scheduler.ThreadUtils;

/**
 * A decorator for a {@link ExecutionEventListener} that is only enabled for the current thread
 * and any thread spawned by the current thread.
 *
 * This listener is used to make sure that only threads under test are controlled by the
 * scheduler.
 */
public class ThreadLocalEventListener extends DelegatingExecutionEventListener {
  private Set<Thread> enabledThreads = Collections.synchronizedSet(new HashSet<Thread>());
  private ThreadLocal<Boolean> inThreadStart = new ThreadLocal<Boolean> () {
    @Override protected Boolean initialValue() {
      return Boolean.FALSE;
    }
  };


  public ThreadLocalEventListener(ExecutionEventListener delegate) {
    super(delegate);
    enabledThreads.add(Thread.currentThread());
  }

  @Override
  protected boolean beforeEvent() {
    return enabled();
  }

  public boolean enabled() {
    return enabledThreads.contains(Thread.currentThread()) && !inThreadStart.get();
  }

  @Override
  public void beforeThreadStart(Thread thread) {
    if(beforeEvent()) {
      inThreadStart.set(Boolean.TRUE);
      enabledThreads.add(thread);
      delegate.beforeThreadStart(thread);
    }
  }

  @Override
  public void afterThreadStart(Thread thread) {
    inThreadStart.set(Boolean.FALSE);
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
