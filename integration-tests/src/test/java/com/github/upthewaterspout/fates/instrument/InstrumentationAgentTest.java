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

package com.github.upthewaterspout.fates.instrument;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.upthewaterspout.fates.core.threading.daemon.DaemonRunnerWithAgent;
import com.github.upthewaterspout.fates.core.threading.event.AtomicClassLoadingDecorator;
import com.github.upthewaterspout.fates.core.threading.event.ExecutionEventListener;
import com.github.upthewaterspout.fates.core.threading.event.ThreadLocalEventListener;
import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventSingleton;
import com.github.upthewaterspout.fates.instrument.instrumented.SampleThreadPark;
import com.github.upthewaterspout.fates.instrument.instrumented.SampleThreadStart;
import com.github.upthewaterspout.fates.instrument.instrumented.SampleWaitNotify;
import org.junit.Test;

/**
 * Tests that the installed java agent is appropriately modifying the bytecode of loaded classes.
 *
 * This contains tests that would ideally be unit tests, but can't be because we can only
 * modify System classes like java.lang.Thread with a real java agent.
 */
public class InstrumentationAgentTest implements Serializable {

  private ExecutionEventListener hook;
  private AtomicInteger fieldAccesses = new AtomicInteger();
  private AtomicInteger beforeThreadStarts = new AtomicInteger();
  private AtomicInteger afterThreadStarts = new AtomicInteger();
  private AtomicInteger beforeThreadExit = new AtomicInteger();
  private AtomicInteger replaceThreadPark = new AtomicInteger();
  private AtomicInteger replaceUnpark = new AtomicInteger();
  private AtomicInteger beforeSynchronization = new AtomicInteger();
  private AtomicInteger afterSynchronization = new AtomicInteger();
  private AtomicInteger replaceWait = new AtomicInteger();
  private AtomicInteger replaceNotify = new AtomicInteger();
  private AtomicInteger replaceNotifyAll = new AtomicInteger();

  public void before() {
    hook = new ExecutionEventListener() {
      @Override
      public void beforeGetField(Object owner, String fieldName, String className,
                                 String methodName,
                                 int lineNumber) {
        fieldAccesses.incrementAndGet();
      }

      @Override
      public void beforeSetField(Object owner, Object fieldValue, String fieldName,
                                 String className,
                                 String methodName,
                                 int lineNumber) {
        fieldAccesses.incrementAndGet();
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
      public void afterNew(Object object) {
        //do nothing
      }

      @Override
      public void postValidation() {
        //do nothing
      }

      @Override
      public void replaceInterrupt(ExecutionEventListener noopHook, Thread thread) {
        //do nothing
      }

      @Override
      public boolean replaceIsInterrupted(ExecutionEventListener noopHook, Thread thread,
                                          boolean clearInterrupt) {
        //do nothing
        return false;
      }

      @Override
      public void beforeThreadStart(Thread thread) {
        beforeThreadStarts.incrementAndGet();

      }

      @Override
      public void afterThreadStart(Thread thread) {
        afterThreadStarts.incrementAndGet();
      }

      @Override public void beforeThreadExit() {
        beforeThreadExit.incrementAndGet();
      }

      @Override public void beforeSynchronization(final Object sync) {
        beforeSynchronization.incrementAndGet();
      }

      @Override public void afterSynchronization(final Object sync) {
        afterSynchronization.incrementAndGet();
      }

      @Override
      public void replaceWait(final ExecutionEventListener defaultAction, final Object sync, final long timeout, int nanos)
          throws InterruptedException {
        defaultAction.replaceWait(null, sync, timeout, nanos);
        replaceWait.incrementAndGet();
      }

      @Override
      public void replaceNotify(final ExecutionEventListener defaultAction, final Object sync) {
        defaultAction.replaceNotify(null, sync);
        replaceNotify.incrementAndGet();

      }

      @Override
      public void replaceNotifyAll(final ExecutionEventListener defaultAction, final Object sync) {
        defaultAction.replaceNotifyAll(null, sync);
        replaceNotifyAll.incrementAndGet();
      }

      @Override
      public void replaceUnpark(final ExecutionEventListener defaultAction,
                                final Thread thread) {
        defaultAction.replaceUnpark(null, thread);
        replaceUnpark.incrementAndGet();

      }

      @Override
      public void replacePark(final ExecutionEventListener defaultAction,
                              final Object blocker) {
        defaultAction.replacePark(null, null);
        replaceThreadPark.incrementAndGet();
      }

      @Override
      public void replaceParkNanos(ExecutionEventListener defaultAction, Object blocker, long timeout) {
        defaultAction.replaceParkNanos(defaultAction, blocker, timeout);
        replaceThreadPark.incrementAndGet();
      }

      @Override
      public void replaceParkUntil(ExecutionEventListener defaultAction, Object blocker, long deadline) {
        defaultAction.replaceParkUntil(defaultAction, blocker, deadline);
        replaceThreadPark.incrementAndGet();
      }

      @Override
      public void replaceJoin(ExecutionEventListener defaultAction, Thread thread, long timeout, int nanos) throws InterruptedException {
        defaultAction.replaceJoin(defaultAction, thread, timeout, nanos);
      }
    };
    ExecutionEventSingleton.setListener(new ThreadLocalEventListener(new AtomicClassLoadingDecorator(hook)));
  }

  public void after() {
    ExecutionEventSingleton.setListener(null);
  }

  @Test
  public void invokingCallWithThreadStartCallsListener() throws Throwable {
    DaemonRunnerWithAgent.execute(() -> {
      before();
      try {
        SampleThreadStart instance = new SampleThreadStart();
        int initialBeforeCount = beforeThreadStarts.get();
        int initialAfterCount = afterThreadStarts.get();
        int initialBeforeExit = beforeThreadExit.get();
        instance.call();
        assertEquals(1, beforeThreadExit.get() - initialBeforeExit);
        assertEquals(1, beforeThreadStarts.get() - initialBeforeCount);
        assertEquals(1, afterThreadStarts.get() - initialAfterCount);
      } finally {
        after();
      }
      return null;
    }, "");
  }

  @Test
  public void invokingCallWithThreadParkCallsListener() throws Throwable {
    DaemonRunnerWithAgent.execute(() -> {
      before();
      try {
        SampleThreadPark instance = new SampleThreadPark();
        int initialPark = replaceThreadPark.get();
        int initialUnpark = replaceUnpark.get();
        instance.call();
        assertEquals(1, replaceThreadPark.get() - initialPark);
        assertEquals(1, replaceUnpark.get() - initialUnpark);
      } finally {
        after();
      }
      return null;
    }, "");
  }

  @Test
  public void invokingCallWithWaitNotifyCallsListener() throws Throwable {
    DaemonRunnerWithAgent.execute(() -> {
      before();
      try {
        //Trigger classloading, etc. which do synchronization before the test
        int initialWait = replaceWait.get();
        int initialNotify = replaceNotify.get();
        new SampleWaitNotify().call();
        assertEquals(1, replaceWait.get() - initialWait);
        assertEquals(1, replaceNotify.get() - initialNotify);
        return null;
      } finally {
        after();
      }
    },"");
  }
}
