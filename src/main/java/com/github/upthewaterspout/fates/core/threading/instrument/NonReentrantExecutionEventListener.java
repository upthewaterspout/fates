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


/**
 * A decorator for {@link ExecutionEventListener} that prevents instrumentation
 * of code that is itself running inside this {@link ExecutionEventListener}
 *
 * This listener delegates to a passed in listener. All of the code in that listener may
 * have bytecode instrumentation, but this listener ensures that for the duration of calls
 * to to the passed in listener, all of that instrumentation is disabled.
 */
public class NonReentrantExecutionEventListener implements ExecutionEventListener {
  private final ExecutionEventListener delegate;

  private final ThreadLocal<Boolean> disabled = new ThreadLocal<Boolean>() {
    @Override
    protected Boolean initialValue() {
      return Boolean.FALSE;
    }
  };

  private RuntimeException lastError;

  public NonReentrantExecutionEventListener(ExecutionEventListener delegate) {
    this.delegate = delegate;
  }

  public void checkForError() {
    RuntimeException error = lastError;
    if(error != null) {
      lastError = null;
      throw error;
    }
  }

  @Override
  public void beforeGetField(String className, String methodName, int lineNumber) {
    if(disabled.get()) {
      return;
    }
    disable();
    try {
      delegate.beforeGetField(className, methodName, lineNumber);
    } catch(RuntimeException t) {
      t.printStackTrace();
      lastError = t;
    } finally {
      enable();
    }
  }

  @Override
  public void beforeSetField(String className, String methodName, int lineNumber) {
    if(disabled.get()) {
      return;
    }
    disable();
    try {
      delegate.beforeSetField(className, methodName, lineNumber);
    } catch(RuntimeException t) {
      t.printStackTrace();
      lastError = t;
    } finally {
      enable();
    }

  }

  @Override
  public void beforeLoadClass() {
    if(disabled.get()) {
      return;
    }
    disable();
    try {
      delegate.beforeLoadClass();
    } catch(RuntimeException t) {
      t.printStackTrace();
      lastError = t;
    } finally {
      enable();
    }
  }

  @Override
  public void afterLoadClass() {
    if(disabled.get()) {
      return;
    }
    disable();
    try {
      delegate.afterLoadClass();
    } catch(RuntimeException t) {
      t.printStackTrace();
      lastError = t;
    } finally {
      enable();
    }
  }

  @Override
  public void beforeThreadStart(Thread thread) {
    if(disabled.get()) {
      return;
    }
    disable();
    try {
      delegate.beforeThreadStart(thread);
    } catch(RuntimeException t) {
      t.printStackTrace();
      lastError = t;
    } finally {
      enable();
    }

  }

  @Override
  public void afterThreadStart(Thread thread) {
    if(disabled.get()) {
      return;
    }
    disable();
    try {
      delegate.afterThreadStart(thread);
    } catch(RuntimeException t) {
      t.printStackTrace();
      lastError = t;
    } finally {
      enable();
    }

  }

  @Override public void beforeThreadExit() {
    if(disabled.get()) {
      return;
    }
    disable();
    try {
      delegate.beforeThreadExit();
    } catch(RuntimeException t) {
      t.printStackTrace();
      lastError = t;
    } finally {
      enable();
    }
  }

  @Override public void beforeSynchronization(final Object sync) {
    if(disabled.get()) {
      return;
    }
    disable();
    try {
      delegate.beforeSynchronization(sync);
    } catch(RuntimeException t) {
      t.printStackTrace();
      lastError = t;
    } finally {
      enable();
    }
  }

  @Override public void afterSynchronization(final Object sync) {
    if(disabled.get()) {
      return;
    }
    disable();
    try {
      delegate.afterSynchronization(sync);
    } catch(RuntimeException t) {
      t.printStackTrace();
      lastError = t;
    } finally {
      enable();
    }
  }

  @Override
  public void replaceWait(final ExecutionEventListener defaultAction, final Object sync, final long timeout, int nanos)
      throws InterruptedException {
    if(disabled.get()) {
      defaultAction.replaceWait(defaultAction, sync, timeout, nanos);
      return;
    }
    disable();
    try {
      delegate.replaceWait(defaultAction, sync, timeout, nanos);
    } catch(RuntimeException t) {
      t.printStackTrace();
      lastError = t;
    } finally {
      enable();
    }
  }

  @Override
  public void replaceNotify(final ExecutionEventListener defaultAction, final Object sync) {
    if(disabled.get()) {
      defaultAction.replaceNotify(defaultAction, sync);
      return;
    }
    disable();
    try {
      delegate.replaceNotify(defaultAction, sync);
    } catch(RuntimeException t) {
      t.printStackTrace();
      lastError = t;
    } finally {
      enable();
    }
  }

  @Override
  public void replaceNotifyAll(final ExecutionEventListener defaultAction, final Object sync) {
    if(disabled.get()) {
      defaultAction.replaceNotifyAll(defaultAction, sync);
      return;
    }
    disable();
    try {
      delegate.replaceNotifyAll(defaultAction, sync);
    } catch(RuntimeException t) {
      t.printStackTrace();
      lastError = t;
    } finally {
      enable();
    }
  }

  @Override
  public void replaceUnpark(final ExecutionEventListener defaultAction, final Thread thread) {
    if(disabled.get()) {
      defaultAction.replaceUnpark(defaultAction, thread);
      return;
    }
    disable();
    try {
      delegate.replaceUnpark(defaultAction, thread);
    } catch(RuntimeException t) {
      t.printStackTrace();
      lastError = t;
    } finally {
      enable();
    }
  }

  @Override
  public void replacePark(final ExecutionEventListener defaultAction, final Object blocker) {
    if(disabled.get()) {
      defaultAction.replacePark(defaultAction, blocker);
      return;
    }
    disable();
    try {
      delegate.replacePark(defaultAction, blocker);
    } catch(RuntimeException t) {
      t.printStackTrace();
      lastError = t;
    } finally {
      enable();
    }

  }

  @Override
  public void replaceJoin(ExecutionEventListener defaultAction, Thread thread, long timeout, int nanos) throws InterruptedException {
    if(disabled.get()) {
      defaultAction.replaceJoin(defaultAction, thread, timeout, nanos);
      return;
    }
    disable();
    try {
      delegate.replaceJoin(defaultAction, thread, timeout, nanos);
    } catch(RuntimeException t) {
      t.printStackTrace();
      lastError = t;
    } finally {
      enable();
    }

  }

  @Override
  public void replaceParkNanos(ExecutionEventListener defaultAction, Object blocker, long timeout) {
    if(disabled.get()) {
      defaultAction.replaceParkNanos(defaultAction, blocker, timeout);
      return;
    }
    disable();
    try {
      delegate.replaceParkNanos(defaultAction, blocker, timeout);
    } catch(RuntimeException t) {
      t.printStackTrace();
      lastError = t;
    } finally {
      enable();
    }

  }

  @Override
  public void replaceParkUntil(ExecutionEventListener defaultAction, Object blocker, long deadline) {
    if(disabled.get()) {
      defaultAction.replaceParkUntil(defaultAction, blocker, deadline);
      return;
    }
    disable();
    try {
      delegate.replaceParkUntil(defaultAction, blocker, deadline);
    } catch(RuntimeException t) {
      t.printStackTrace();
      lastError = t;
    } finally {
      enable();
    }

  }


  public void disable() {
    disabled.set(Boolean.TRUE);
  }

  public void enable() {
    disabled.set(Boolean.FALSE);
  }

}
