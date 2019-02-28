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

package com.github.upthewaterspout.fates.core.threading.instrument;

import com.github.upthewaterspout.fates.core.threading.event.ExecutionEventListener;
import com.github.upthewaterspout.fates.core.threading.event.NonReentrantExecutionEventListener;
import com.github.upthewaterspout.fates.core.threading.event.NoopExecutionEventListener;

/**
 * Static methods that should be called from instrumented bytecode to control the execution of
 * code.
 *
 * All instrumentation of bytecode ends up creating calls to static methods in this class. This
 * class is responsible for taking those calls and passing them to a single, installed {@link
 * ExecutionEventListener} which is installed by {@link #setListener(ExecutionEventListener)}
 *
 * This class will atomically decorate the passed in listener with a {@link
 * NonReentrantExecutionEventListener}
 */
public class ExecutionEventSingleton {

  private static volatile boolean available = false;

  private static final ExecutionEventListener NOOP_HOOK = new NoopExecutionEventListener();
  private static ExecutionEventListener instance = NOOP_HOOK;

  public static boolean setAvailable() {
    return setAvailable(true);
  }

  public static boolean setAvailable(boolean value) {
    boolean oldValue = available;
    available = value;
    return oldValue;
  }

  public static boolean isAvailable() {
    return available;
  }

  public static void setListener(ExecutionEventListener hook) {
    if(!available) {
      throw new IllegalStateException("No instrumentation agent registered");
    }
    if(instance instanceof NonReentrantExecutionEventListener) {
      ((NonReentrantExecutionEventListener) instance).checkForError();
    }

    if(hook == null) {
      instance = NOOP_HOOK;
    } else {
      instance = new NonReentrantExecutionEventListener(hook);
    }
  }

  public static void beforeGetField(Object owner, String fieldName, String className, String methodName, int lineNumber) {
    instance.beforeGetField(owner, fieldName, className, methodName, lineNumber);
  }

  public static void beforeSetField(Object owner, Object fieldValue, String fieldName, String className, String methodName, int lineNumber) {
    instance.beforeSetField(owner, fieldValue, fieldName, className, methodName, lineNumber);
  }

  public static void beforeThreadStart(Thread thread) {
    instance.beforeThreadStart(thread);
  }

  public static void afterThreadStart(Thread thread) {
    instance.afterThreadStart(thread);
  }

  public static void beforeThreadExit() {
    instance.beforeThreadExit();
  }

  public static void replacePark() {
    instance.replacePark(NOOP_HOOK, null);
  }

  public static void replacePark(Object blocker) {
    instance.replacePark(NOOP_HOOK, blocker);
  }

  public static void replaceParkNanos(long time) {
    instance.replaceParkNanos(NOOP_HOOK, null, time);
  }

  public static void replaceParkNanos(Object blocker, long time) {
    instance.replaceParkNanos(NOOP_HOOK, blocker, time);
  }

  public static void replaceParkUntil(long deadline) {
    instance.replaceParkUntil(NOOP_HOOK, null, deadline);
  }

  public static void replaceParkUntil(Object blocker, long deadline) {
    instance.replaceParkUntil(NOOP_HOOK, blocker, deadline);
  }

  public static void replaceUnpark(Thread thread) {
    instance.replaceUnpark(NOOP_HOOK, thread);
  }

  public static void beforeSynchronization(final Object sync) {
    instance.beforeSynchronization(sync);
  }

  public static void afterSynchronization(final Object sync) {
    instance.afterSynchronization(sync);
  }

  public static void replaceWait(final Object sync) throws InterruptedException {
    instance.replaceWait(NOOP_HOOK, sync, 0, 0);
  }

  public static void replaceWait(final Object sync, final long timeout) throws InterruptedException {
    instance.replaceWait(NOOP_HOOK, sync, timeout, 0);
  }

  public static void replaceWait(final Object sync, final long timeout, final int nanos) throws InterruptedException {
    instance.replaceWait(NOOP_HOOK, sync, timeout, nanos);
  }

  public static void replaceNotify(final Object sync) {
    instance.replaceNotify(NOOP_HOOK, sync);

  }

  public static void replaceNotifyAll(final Object sync) {
    instance.replaceNotifyAll(NOOP_HOOK, sync);
  }

  public static void replaceJoin(Thread thread) throws InterruptedException {
    instance.replaceJoin(NOOP_HOOK, thread, 0, 0);
  }

  public static void replaceJoin(Thread thread, final long timeout) throws InterruptedException {
    instance.replaceJoin(NOOP_HOOK, thread, timeout, 0);
  }

  public static void replaceJoin(Thread thread, final long timeout, final int nanos) throws InterruptedException {
    instance.replaceJoin(NOOP_HOOK, thread, timeout, nanos);
  }

  public static void beforeMethod(String className, String methodName) {
    instance.beforeMethod(className, methodName);
  }

  public static void afterMethod(String className, String methodName) {
    instance.afterMethod(className, methodName);
  }

  public static void afterNew(Object object) {
    instance.afterNew(object);
  }
}
