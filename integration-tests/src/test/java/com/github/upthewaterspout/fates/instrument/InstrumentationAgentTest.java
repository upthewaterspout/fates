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

import com.github.upthewaterspout.fates.core.threading.harness.ThreadLocalEventListener;
import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventListener;
import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventSingleton;
import com.github.upthewaterspout.fates.instrument.instrumented.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * Tests that the installed java agent is appropriately modifying the bytecode of loaded classes.
 *
 * This contains tests that would ideally be unit tests, but can't be because we can only
 * modify System classes like java.lang.Thread with a real java agent.
 */
public class InstrumentationAgentTest {

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

  @Before
  public void before() {
    hook = new ExecutionEventListener() {
      @Override
      public void beforeGetField(String className, String methodName, int lineNumber) {
        fieldAccesses.incrementAndGet();
      }

      @Override
      public void beforeSetField(String className, String methodName, int lineNumber) {
        fieldAccesses.incrementAndGet();
      }

      @Override
      public void beforeLoadClass() {
        //do nothing
      }

      @Override
      public void afterLoadClass() {
        //do nothing
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
    ExecutionEventSingleton.setListener(new ThreadLocalEventListener(hook));
  }

  @After
  public void after() {
    ExecutionEventSingleton.setListener(null);
  }

  @Test
  public void invokingCallWithFieldAccessCallsListener() {
    SampleFieldAccess instance = new SampleFieldAccess();
    int initialCount = fieldAccesses.get();
    instance.call();
    int finalCount = fieldAccesses.get();
    assertEquals(4, finalCount - initialCount);
  }

  @Test
  public void invokingCallWithThreadStartCallsListener() throws Exception {
    SampleThreadStart instance = new SampleThreadStart();
    int initialBeforeCount = beforeThreadStarts.get();
    int initialAfterCount = afterThreadStarts.get();
    int initialBeforeExit = beforeThreadExit.get();
    instance.call();
    assertEquals(1, beforeThreadExit.get() - initialBeforeExit);
    assertEquals(1, beforeThreadStarts.get() - initialBeforeCount);
    assertEquals(1, afterThreadStarts.get() - initialAfterCount);
  }

  @Test
  public void invokingCallWithThreadParkCallsListener() throws Exception {
    SampleThreadPark instance = new SampleThreadPark();
    int initialPark = replaceThreadPark.get();
    int initialUnpark = replaceUnpark.get();
    instance.call();
    assertEquals(1, replaceThreadPark.get() - initialPark);
    assertEquals(1, replaceUnpark.get() - initialUnpark);
  }

  @Test
  public void invokingCallWithSynchronizationCallsListener() throws Exception {
    //Trigger classloading, etc. which do synchronization before the test
    SampleSynchronization.testNormalSync();
    int initialBeforeSync = beforeSynchronization.get();
    int initialAfterSync = afterSynchronization.get();
    SampleSynchronization.testNormalSync();
    assertEquals(1, beforeSynchronization.get() - initialBeforeSync);
    assertEquals(1, afterSynchronization.get() - initialAfterSync);
  }

  @Test
  public void invokingCallWithWaitNotifyCallsListener() throws Exception {
    //Trigger classloading, etc. which do synchronization before the test
    int initialWait = replaceWait.get();
    int initialNotify = replaceNotify.get();
    new SampleWaitNotify().call();
    assertEquals(1, replaceWait.get() - initialWait);
    assertEquals(1, replaceNotify.get() - initialNotify);
  }
}
