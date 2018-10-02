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
 * A decorator for a {@link ExecutionEventListener} that allows disabling the
 * {@link ExecutionEventListener#beforeGetField(String, String, String, int)} and {@link ExecutionEventListener#beforeSetField(String, String, String, int)}
 * calls inside of an atomic block.
 *
 * To use, call
 * beginAtomic()
 * try {
 *   //Your code here
 * } finally {
 *   endAtomic();
 * }
 *
 * This class is reentrant, so the number of endAtomic calls must
 * match the number of beginAtomic calls before the scheduler will yield again.
 *
 * This decorator is used to suppress context switching that would normally happen from field
 * accesses within this atomic block, to reduce the number of decision points in a test.
 */
public class ExecutionEventListenerWithAtomicControl
    implements ExecutionEventListener, AtomicControl {
  private ExecutionEventListener delegate;

  private ThreadLocal<EntryCount> atomicEntryCount = new ThreadLocal<EntryCount> () {
    @Override protected EntryCount initialValue() {
      return new EntryCount();
    }
  };

  public ExecutionEventListenerWithAtomicControl(final ExecutionEventListener delegate) {
    this.delegate = delegate;
  }

  public void afterThreadStart(final Thread thread) {
    delegate.afterThreadStart(thread);
  }

  @Override
  public void beforeThreadExit() {
   delegate.beforeThreadExit();
  }

  @Override
  public void replacePark(
      ExecutionEventListener defaultAction,
      Object blocker) {
    delegate.replacePark(defaultAction, blocker);
  }

  @Override
  public void replaceParkNanos(
      ExecutionEventListener defaultAction,
      Object blocker, long time) {
    delegate.replaceParkNanos(defaultAction, blocker, time);
  }

  @Override
  public void replaceParkUntil(
      ExecutionEventListener defaultAction,
      Object blocker, long time) {
    delegate.replaceParkUntil(defaultAction, blocker, time);
  }

  @Override
  public void replaceUnpark(ExecutionEventListener defaultAction, final Thread thread) {
    delegate.replaceUnpark(defaultAction, thread);

  }

  @Override
  public void replaceWait(
      ExecutionEventListener defaultAction,
      final Object sync, long timeout, int nanos) throws InterruptedException {
    delegate.replaceWait(defaultAction, sync, timeout, nanos);

  }

  @Override
  public void replaceNotify(
      ExecutionEventListener defaultAction,
      final Object sync) {
    delegate.replaceNotify(defaultAction, sync);
  }

  @Override
  public void replaceNotifyAll(
      ExecutionEventListener defaultAction,
      final Object sync) {
    delegate.replaceNotifyAll(defaultAction, sync);
  }

  @Override
  public void beforeSynchronization(final Object sync) {
    delegate.beforeSynchronization(sync);
  }

  @Override
  public void afterSynchronization(final Object sync) {
    delegate.afterSynchronization(sync);
  }

  @Override
  public void replaceJoin(ExecutionEventListener defaultAction, Thread thread, long timeout,
                          int nanos) throws InterruptedException {
    delegate.replaceJoin(defaultAction, thread, timeout, nanos);
  }

  public void beginAtomic() {
    atomicEntryCount.get().increment();
  }

  public void endAtomic() {
    atomicEntryCount.get().decrement();
  }

  @Override
  public void beforeGetField(String className, String methodName, int lineNumber) {
    if(atomicEntryCount.get().isZero()) {
      delegate.beforeGetField(className, methodName, lineNumber);
    }

  }

  @Override
  public void beforeSetField(String className, String methodName, int lineNumber) {
    if(atomicEntryCount.get().isZero()) {
      delegate.beforeSetField(className, methodName, lineNumber);
    }
  }

  public void beforeThreadStart(final Thread thread) {
    delegate.beforeThreadStart(thread);
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
