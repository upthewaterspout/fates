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

package com.github.upthewaterspout.fates.core.threading.harness;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventListener;

/**
 * A decorator for a {@link ExecutionEventListener} that is only enabled for the current thread
 * and any thread spawned by the current thread.
 *
 * This listener is used to make sure that only threads under test are controlled by the
 * scheduler.
 */
public class ThreadLocalEventListener implements ExecutionEventListener {
  private Set<Thread> enabledThreads = Collections.synchronizedSet(new HashSet<Thread>());
  private ThreadLocal<Boolean> inThreadStart = new ThreadLocal<Boolean> () {
    @Override protected Boolean initialValue() {
      return Boolean.FALSE;
    }
  };

  private final ExecutionEventListener delegate;

  public ThreadLocalEventListener(ExecutionEventListener delegate) {
    this.delegate = delegate;
    enabledThreads.add(Thread.currentThread());
  }

  public boolean enabled() {
    return enabledThreads.contains(Thread.currentThread()) && !inThreadStart.get();
  }

  @Override
  public void beforeGetField(Object owner, String className, String methodName,
                             int lineNumber) {
    if(enabled()) {
      delegate.beforeGetField(owner, className, methodName, lineNumber);
    }
  }

  @Override
  public void beforeSetField(Object owner, Object fieldValue, String className,
                             String methodName,
                             int lineNumber) {
    if(enabled()) {
      delegate.beforeSetField(owner, fieldValue, className, methodName, lineNumber);
    }
  }

  @Override
  public void beforeLoadClass() {
    if(enabled()) {
      delegate.beforeLoadClass();
    }
  }

  @Override
  public void afterLoadClass() {
    if(enabled()) {
      delegate.afterLoadClass();
    }
  }

  @Override
  public void beforeThreadStart(Thread thread) {
    if(enabled()) {
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

  @Override public void beforeThreadExit() {
    if(enabled()) {
      enabledThreads.remove(Thread.currentThread());
      delegate.beforeThreadExit();
    }
  }

  @Override public void beforeSynchronization(final Object sync) {
    if(enabled()) {
      delegate.beforeSynchronization(sync);
    }

  }

  @Override public void afterSynchronization(final Object sync) {
    if(enabled()) {
      delegate.afterSynchronization(sync);
    }
  }

  @Override
  public void replaceWait(final ExecutionEventListener defaultAction, final Object sync, final long timeout, int nanos)
      throws InterruptedException {

    if(enabled()) {
      delegate.replaceWait(defaultAction, sync, timeout, nanos);
    } else {
      defaultAction.replaceWait(defaultAction, sync, timeout, nanos);
    }
  }

  @Override
  public void replaceNotify(final ExecutionEventListener defaultAction, final Object sync) {

    if(enabled()) {
      delegate.replaceNotify(defaultAction, sync);
    } else {
      defaultAction.replaceNotify(defaultAction, sync);
    }
  }

  @Override
  public void replaceNotifyAll(final ExecutionEventListener defaultAction, final Object sync) {
    if(enabled()) {
      delegate.replaceNotifyAll(defaultAction, sync);
    } else {
      defaultAction.replaceNotifyAll(defaultAction, sync);
    }
  }

  @Override
  public void replaceUnpark(final ExecutionEventListener defaultAction, final Thread thread) {
    if(enabled()) {
      delegate.replaceUnpark(defaultAction, thread);
    } else {
      defaultAction.replaceUnpark(defaultAction, thread);
    }
  }

  @Override
  public void replacePark(final ExecutionEventListener defaultAction, final Object blocker) {
    if(enabled()) {
      delegate.replacePark(defaultAction, blocker);
    } else {
      defaultAction.replacePark(defaultAction, blocker);
    }
  }

  @Override
  public void replaceParkNanos(final ExecutionEventListener defaultAction, final Object blocker,
                               final long timeout) {
    if (enabled()) {
      delegate.replaceParkNanos(defaultAction, blocker, timeout);
    } else {
      defaultAction.replaceParkNanos(defaultAction, blocker, timeout);
    }
  }

  @Override
  public void replaceParkUntil(final ExecutionEventListener defaultAction, final Object blocker,
                               long deadline) {
    if (enabled()) {
      delegate.replaceParkUntil(defaultAction, blocker, deadline);
    } else {
      defaultAction.replaceParkUntil(defaultAction, blocker, deadline);
    }
  }

  @Override
  public void replaceJoin(ExecutionEventListener defaultAction, Thread thread, long timeout,
                          int nanos) throws InterruptedException {
    if (enabled()) {
      delegate.replaceJoin(defaultAction, thread, timeout, nanos);
    } else {
      defaultAction.replaceJoin(defaultAction, thread, timeout, nanos);
    }
  }

  @Override
  public void afterNew(Object object) {
    if (enabled()) {
      delegate.afterNew(object);
    }
  }
}
