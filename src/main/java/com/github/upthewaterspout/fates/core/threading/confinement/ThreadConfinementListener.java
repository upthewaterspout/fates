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

package com.github.upthewaterspout.fates.core.threading.confinement;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;

import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventListener;

public class ThreadConfinementListener implements ExecutionEventListener {
  private final ExecutionEventListener delegate;
  private ReachableObjectFinder reachableObjects = new ReachableObjectFinder();

  private ThreadLocal<Set<Object>> threadConfinedObjects = ThreadLocal.withInitial(() -> Collections
      .newSetFromMap(new IdentityHashMap<>()));

  public ThreadConfinementListener(
      ExecutionEventListener listener) {
    this.delegate = listener;
  }

  @Override
  public void beforeThreadStart(Thread thread) {
    delegate.beforeThreadStart(thread);

  }

  @Override
  public void afterThreadStart(Thread thread) {
    delegate.afterThreadStart(thread);

  }

  @Override
  public void beforeThreadExit() {
    delegate.beforeThreadExit();
  }

  @Override
  public void replaceJoin(ExecutionEventListener defaultAction, Thread thread, long timeout,
                          int nanos) throws InterruptedException {
    delegate.replaceJoin(defaultAction, thread, timeout, nanos);

  }

  @Override
  public void replacePark(ExecutionEventListener defaultAction, Object blocker) {
    delegate.replacePark(defaultAction, blocker);

  }

  @Override
  public void replaceParkNanos(ExecutionEventListener defaultAction, Object blocker, long timeout) {
    delegate.replaceParkNanos(defaultAction, blocker, timeout);
  }

  @Override
  public void replaceParkUntil(ExecutionEventListener defaultAction, Object blocker,
                               long deadline) {
    delegate.replaceParkUntil(defaultAction, blocker, deadline);
  }

  @Override
  public void replaceUnpark(ExecutionEventListener defaultAction, Thread thread) {
    delegate.replaceUnpark(defaultAction, thread);
  }

  @Override
  public void replaceWait(ExecutionEventListener defaultAction, Object sync, long timeout,
                          int nanos) throws InterruptedException {
    delegate.replaceWait(defaultAction, sync, timeout, nanos);
  }

  @Override
  public void replaceNotify(ExecutionEventListener defaultAction, Object sync) {
    delegate.replaceNotify(defaultAction, sync);
  }

  @Override
  public void replaceNotifyAll(ExecutionEventListener defaultAction, Object sync) {
    delegate.replaceNotifyAll(defaultAction, sync);
  }

  @Override
  public void beforeSynchronization(Object sync) {
    delegate.beforeSynchronization(sync);
  }

  @Override
  public void afterSynchronization(Object sync) {
    delegate.afterSynchronization(sync);
  }

  @Override
  public void beforeGetField(Object owner, String className, String methodName, int lineNumber) {
    if(threadConfinedObjects.get().contains(owner)) {
      //Do nothing if the object is confined to this thread
      return;
    }
    delegate.beforeGetField(owner, className, methodName, lineNumber);
  }

  @Override
  public void beforeSetField(Object owner, Object fieldValue, String className, String methodName,
                             int lineNumber) {
    if(threadConfinedObjects.get().contains(owner)) {
      //Do nothing if the object is confined to this thread
      return;
    }

    removeThreadConfinedObject(fieldValue);
    delegate.beforeSetField(owner, fieldValue, className, methodName, lineNumber);
  }

  private void removeThreadConfinedObject(Object fieldValue) {
    Set<Object> threadLocalObjects = threadConfinedObjects.get();
    reachableObjects.stream(fieldValue, threadLocalObjects::contains)
        .forEach(threadLocalObjects::remove);
  }

  @Override
  public void beforeLoadClass() {
    delegate.beforeLoadClass();
  }

  @Override
  public void afterLoadClass() {
    delegate.afterLoadClass();
  }

  @Override
  public void afterNew(Object object) {
    threadConfinedObjects.get().add(object);
    delegate.afterNew(object);
  }
}
