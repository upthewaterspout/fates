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

import com.github.upthewaterspout.fates.core.threading.event.ExecutionEventListener;

/**
 * An {@link ExecutionEventListener} that delegates all calls to a nested
 * {@link ExecutionEventListener}.
 *
 * Sublasses can override {@link #beforeEvent()} to supress delegation
 * of method calls.
 */
public abstract class DelegatingExecutionEventListener implements ExecutionEventListener {
  protected ExecutionEventListener delegate;

  public DelegatingExecutionEventListener(
      final ExecutionEventListener delegate) {
    this.delegate = delegate;
  }

  public void afterThreadStart(final Thread thread) {
    if(beforeEvent()) {
      delegate.afterThreadStart(thread);
    }
  }

  /**
   * Called before an event invocation.
   * @return True if the event should not be passed on to the delegate
   */
  protected boolean beforeEvent() {
    // do nothing
    return true;
  }

  public void beforeThreadExit() {
    if(beforeEvent()) {
      delegate.beforeThreadExit();
    }
  }

  public void beforeMethod(String className, String methodName) {
    if(beforeEvent()) {
      delegate.beforeMethod(className, methodName);
    }
  }

  public void afterMethod(String className, String methodName) {
    if(beforeEvent()) {
      delegate.afterMethod(className, methodName);
    }
  }

  public void replacePark(
      ExecutionEventListener defaultAction,
      Object blocker) {
    if(beforeEvent()) {
      delegate.replacePark(defaultAction, blocker);
    } else {
      defaultAction.replacePark(defaultAction, blocker);
    }
  }

  public void replaceParkNanos(
      ExecutionEventListener defaultAction,
      Object blocker, long time) {
    if(beforeEvent()) {
      delegate.replaceParkNanos(defaultAction, blocker, time);
    } else {
      defaultAction.replaceParkNanos(defaultAction,blocker,time);
    }
  }

  public void replaceParkUntil(
      ExecutionEventListener defaultAction,
      Object blocker, long time) {
    if(beforeEvent()) {
      delegate.replaceParkUntil(defaultAction, blocker, time);
    } else {
      defaultAction.replaceParkUntil(defaultAction, blocker, time);
    }
  }

  public void replaceUnpark(ExecutionEventListener defaultAction, final Thread thread) {
    if(beforeEvent()) {
      delegate.replaceUnpark(defaultAction, thread);
    } else {
      defaultAction.replaceUnpark(defaultAction, thread);
    }

  }

  public void replaceWait(
      ExecutionEventListener defaultAction,
      final Object sync, long timeout, int nanos) throws InterruptedException {
    if(beforeEvent()) {
      delegate.replaceWait(defaultAction, sync, timeout, nanos);
    } else {
      defaultAction.replaceWait(defaultAction, sync, timeout, nanos);
    }

  }

  public void replaceNotify(
      ExecutionEventListener defaultAction,
      final Object sync) {
    if(beforeEvent()) {
      delegate.replaceNotify(defaultAction, sync);
    } else {
      defaultAction.replaceNotify(defaultAction, sync);
    }
  }

  public void replaceNotifyAll(
      ExecutionEventListener defaultAction,
      final Object sync) {
    if(beforeEvent()) {
      delegate.replaceNotifyAll(defaultAction, sync);
    } else {
      defaultAction.replaceNotifyAll(defaultAction, sync);
    }
  }

  public void beforeSynchronization(final Object sync) {
    if(beforeEvent()) {
      delegate.beforeSynchronization(sync);
    }
  }

  public void afterSynchronization(final Object sync) {
    if(beforeEvent()) {
      delegate.afterSynchronization(sync);
    }
  }

  public void replaceJoin(ExecutionEventListener defaultAction, Thread thread, long timeout,
                          int nanos) throws InterruptedException {
    if(beforeEvent()) {
      delegate.replaceJoin(defaultAction, thread, timeout, nanos);
    } else {
      defaultAction.replaceJoin(defaultAction, thread, timeout, nanos);
    }
  }

  public void beforeGetField(Object owner, String fieldName, String className,
                             String methodName,
                             int lineNumber) {
    if(beforeEvent()) {
      delegate.beforeGetField(owner, fieldName, className, methodName, lineNumber);
    }

  }

  public void beforeSetField(Object owner, Object fieldValue, String fieldName,
                             String className,
                             String methodName,
                             int lineNumber) {
    if(beforeEvent()) {
      delegate.beforeSetField(owner, fieldValue, fieldName, className, methodName, lineNumber);
    }
  }

  public void afterNew(Object object) {
    if(beforeEvent()) {
      delegate.afterNew(object);
    }
  }

  public void beforeThreadStart(final Thread thread) {
    if(beforeEvent()) {
      delegate.beforeThreadStart(thread);
    }
  }
}
