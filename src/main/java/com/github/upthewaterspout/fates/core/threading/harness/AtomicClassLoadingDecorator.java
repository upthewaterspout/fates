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

import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventListener;

/**
 * A decorator for a {@link ExecutionEventListener} that allows disabling the {@link
 * ExecutionEventListener#beforeGetField(String, String, int)}  and {@link
 * ExecutionEventListener#beforeSetField(Object, Object, String, String, int)} while class loading is happening.
 *
 * This decorator is used to suppress context switching that would normally happen from field
 * accesses within classloading, to reduce the number of decision points in a test.
 */
public class AtomicClassLoadingDecorator
    implements ExecutionEventListener {
  private ExecutionEventListener delegate;

  private ThreadLocal<EntryCount> atomicEntryCount = new ThreadLocal<EntryCount> () {
    @Override protected EntryCount initialValue() {
      return new EntryCount();
    }
  };

  public AtomicClassLoadingDecorator(final ExecutionEventListener delegate) {
    this.delegate = delegate;
  }

  public void afterThreadStart(final Thread thread) {
    if(atomicEntryCount.get().isZero()) {
      delegate.afterThreadStart(thread);
    }
  }

  @Override
  public void beforeThreadExit() {
    if(atomicEntryCount.get().isZero()) {
      delegate.beforeThreadExit();
    }
  }

  @Override
  public void replacePark(
      ExecutionEventListener defaultAction,
      Object blocker) {
    if(atomicEntryCount.get().isZero()) {
      delegate.replacePark(defaultAction, blocker);
    } else {
      defaultAction.replacePark(defaultAction, blocker);
    }
  }

  @Override
  public void replaceParkNanos(
      ExecutionEventListener defaultAction,
      Object blocker, long time) {
    if(atomicEntryCount.get().isZero()) {
      delegate.replaceParkNanos(defaultAction, blocker, time);
    } else {
      defaultAction.replaceParkNanos(defaultAction,blocker,time);
    }
  }

  @Override
  public void replaceParkUntil(
      ExecutionEventListener defaultAction,
      Object blocker, long time) {
    if(atomicEntryCount.get().isZero()) {
      delegate.replaceParkUntil(defaultAction, blocker, time);
    } else {
      defaultAction.replaceParkUntil(defaultAction, blocker, time);
    }
  }

  @Override
  public void replaceUnpark(ExecutionEventListener defaultAction, final Thread thread) {
    if(atomicEntryCount.get().isZero()) {
      delegate.replaceUnpark(defaultAction, thread);
    } else {
      defaultAction.replaceUnpark(defaultAction, thread);
    }

  }

  @Override
  public void replaceWait(
      ExecutionEventListener defaultAction,
      final Object sync, long timeout, int nanos) throws InterruptedException {
    if(atomicEntryCount.get().isZero()) {
      delegate.replaceWait(defaultAction, sync, timeout, nanos);
    } else {
      defaultAction.replaceWait(defaultAction, sync, timeout, nanos);
    }

  }

  @Override
  public void replaceNotify(
      ExecutionEventListener defaultAction,
      final Object sync) {
    if(atomicEntryCount.get().isZero()) {
      delegate.replaceNotify(defaultAction, sync);
    } else {
      defaultAction.replaceNotify(defaultAction, sync);
    }
  }

  @Override
  public void replaceNotifyAll(
      ExecutionEventListener defaultAction,
      final Object sync) {
    if(atomicEntryCount.get().isZero()) {
      delegate.replaceNotifyAll(defaultAction, sync);
    } else {
      defaultAction.replaceNotifyAll(defaultAction, sync);
    }
  }

  @Override
  public void beforeSynchronization(final Object sync) {
    if(atomicEntryCount.get().isZero()) {
      delegate.beforeSynchronization(sync);
    }
  }

  @Override
  public void afterSynchronization(final Object sync) {
    if(atomicEntryCount.get().isZero()) {
      delegate.afterSynchronization(sync);
    }
  }

  @Override
  public void replaceJoin(ExecutionEventListener defaultAction, Thread thread, long timeout,
                          int nanos) throws InterruptedException {
    if(atomicEntryCount.get().isZero()) {
      delegate.replaceJoin(defaultAction, thread, timeout, nanos);
    } else {
      defaultAction.replaceJoin(defaultAction, thread, timeout, nanos);
    }
  }

  @Override
  public void beforeGetField(String className, String methodName, int lineNumber) {
    if(atomicEntryCount.get().isZero()) {
      delegate.beforeGetField(className, methodName, lineNumber);
    }

  }

  @Override
  public void beforeSetField(Object owner, Object fieldValue, String className,
                             String methodName,
                             int lineNumber) {
    if(atomicEntryCount.get().isZero()) {
      delegate.beforeSetField(owner, fieldValue, className, methodName, lineNumber);
    }
  }

  @Override
  public void beforeLoadClass() {
    beginAtomic();
    delegate.beforeLoadClass();
  }

  @Override
  public void afterLoadClass() {
    delegate.afterLoadClass();
    endAtomic();
  }

  public void beforeThreadStart(final Thread thread) {
    if(atomicEntryCount.get().isZero()) {
      delegate.beforeThreadStart(thread);
    }
  }

  private void beginAtomic() {
    atomicEntryCount.get().increment();
  }

  private void endAtomic() {
    atomicEntryCount.get().decrement();
  }


  private static class EntryCount {
    private int count;

    public void increment() {
      count++;
    }

    public void decrement() {
      count--;
    }

    public boolean isZero() {
      return count <= 0;
    }
  }
}
