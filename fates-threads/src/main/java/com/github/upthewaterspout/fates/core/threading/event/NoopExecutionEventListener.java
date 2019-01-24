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

import java.util.concurrent.locks.LockSupport;

import com.github.upthewaterspout.fates.core.threading.event.ExecutionEventListener;

/**
 * An {@link ExecutionEventListener} that behaves as though the code has not been instrumented.
 *
 * For the before and after methods, this means this listener does nothing. However, for the replace
 * methods such as {@link #replacePark(ExecutionEventListener, Object)}, this listener performs the
 * actual JDK operation, for example by parking the thread in the case of {@link
 * #replacePark(ExecutionEventListener, Object)}
 */
public class NoopExecutionEventListener implements ExecutionEventListener {
  @Override
  public void beforeGetField(Object owner, String fieldName, String className,
                             String methodName,
                             int lineNumber) {
    //do nothing
  }

  @Override
  public void beforeSetField(Object owner, Object fieldValue, String fieldName,
                             String className,
                             String methodName,
                             int lineNumber) {
    //do nothing
  }

  @Override
  public void beforeMethod(String className, String methodName) {
    //do nothing
  }

  @Override
  public void afterMethod(String className, String methodName) {
    //do nothing
  }

  @Override
  public void beforeThreadStart(Thread thread) {
    //do nothing
  }

  @Override
  public void afterThreadStart(Thread thread) {
    //do nothing
  }

  @Override public void beforeThreadExit() {

  }

  @Override public void beforeSynchronization(final Object sync) {

  }

  @Override public void afterSynchronization(final Object sync) {

  }

  @Override
  public void replaceNotify(final ExecutionEventListener defaultAction, final Object sync) {
    sync.notify();
  }

  @Override
  public void replaceWait(final ExecutionEventListener defaultAction, final Object sync, final long timeout, int nanos)
      throws InterruptedException {
    if(nanos == 0) {
      sync.wait(timeout);
    } else {
      sync.wait(timeout, nanos);
    }
  }

  @Override
  public void replaceUnpark(final ExecutionEventListener defaultAction, final Thread thread) {
    LockSupport.unpark(thread);

  }

  @Override
  public void replacePark(final ExecutionEventListener defaultAction, final Object blocker) {
    LockSupport.park(blocker);
  }

  @Override
  public void replaceNotifyAll(final ExecutionEventListener defaultAction, final Object sync) {
    sync.notifyAll();
  }

  @Override
  public void replaceJoin(ExecutionEventListener defaultAction, Thread thread, long timeout, int nanos) throws InterruptedException {
    if(nanos == 0) {
      thread.join(timeout);
    } else {
      thread.join(timeout, nanos);
    }
  }

  @Override
  public void replaceParkNanos(ExecutionEventListener defaultAction, Object blocker, long timeout) {
    LockSupport.parkNanos(blocker, timeout);
  }

  @Override
  public void replaceParkUntil(ExecutionEventListener defaultAction, Object blocker, long deadline) {
    LockSupport.parkUntil(blocker, deadline);
  }

  @Override
  public void afterNew(Object object) {

  }
}
