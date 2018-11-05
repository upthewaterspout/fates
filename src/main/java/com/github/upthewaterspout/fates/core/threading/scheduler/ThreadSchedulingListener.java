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

package com.github.upthewaterspout.fates.core.threading.scheduler;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.github.upthewaterspout.fates.core.states.Decider;
import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventListener;
import sun.misc.Unsafe;

/**
 * An {@link ExecutionEventListener} that actually controls which threads are allowed
 * to proceed during an execution event. This class, in combination with {@link SchedulerState},
 * does the majority of the work to control which threads are actually running.
 *
 * This class mostly manages concurrency control of the actual thread calls. Each
 * thread when it enters this class with either be allowed to proceed or forced to wait on
 * a {@link Condition}. Only one thread is allowed to proceed at a time. If the test ordering calls
 * for a change of threads, the existing thread will notify the newly scheduled thread and then
 * block itself on it's on {@link Condition}
 *
 * All state related to tracking threads and deciding what threads should be scheduled belongs in
 * {@link SchedulerState}
 */
public class ThreadSchedulingListener implements ExecutionEventListener {
  private final SchedulerState schedulerState;

  /**
   * A lock used to guard the state of this scheduler
   */
  private final ReentrantLock lock = new ReentrantLock();

  /**
   * A condition for each thread, used to have threads wait if they are not currently scheduled
   */
  private final Map<Thread, Condition> threadConditions = new HashMap<>();

  /**
   * An interface for controlling java object monitors. This is usually just a wrapper around
   * unsafe, but it is overridable for unit testing.
   */
  private final MonitorControl monitorControl;


  public ThreadSchedulingListener(Decider decider) {
    this(decider, new DefaultMonitorControl());
  }

  public ThreadSchedulingListener(Decider decider, MonitorControl monitorControl) {
    this.schedulerState = new SchedulerState(decider);
    this.monitorControl = monitorControl;
  }


  /**
   * Begin a new test. The current thread is marked as part of the threads under test, and
   * allowed to proceed.
   */
  public void begin() {
    lock.lock();
    try {
      Thread currentThread = Thread.currentThread();
      threadConditions.put(currentThread, lock.newCondition());
      schedulerState.newThread(currentThread, null);
    } finally {
      lock.unlock();
    }
  }

  /**
   * Indicate that a new thread is starting. The new thread is added to the threads under
   * test, but the current thread is allowed to proceed
   */
  @Override
  public void beforeThreadStart(final Thread thread) {
    lock.lock();
    try {
      threadConditions.put(thread, lock.newCondition());
      schedulerState.newThread(thread, Thread.currentThread());
    } finally {
      lock.unlock();
    }
  }

  /**
   * After starting a new thread, potentially block the current thread an allow the thread
   * to proceed.
   */
  @Override
  public void afterThreadStart(Thread thread) {
    yield();
  }

  /**
   * When a thread exits, remove it from the threads under test and pick a new thread to proceed
   */
  @Override
  public void beforeThreadExit() {
    lock.lock();
    try {
      Thread nextThread = schedulerState.threadTerminated(Thread.currentThread());
      notify(nextThread);
    } finally {
      lock.unlock();
    }
  }

  /**
   * Before a field read, potentially switch to a new thread
   */
  @Override
  public void beforeGetField(String className, String methodName, int lineNumber) {
    schedulerState.setLineNumber(Thread.currentThread(), className, methodName, lineNumber);
    yield();
  }

  /**
   * Before a field write, potentially switch to a new thread
   */
  @Override
  public void beforeSetField(Object owner, Object fieldValue, String className,
                             String methodName,
                             int lineNumber) {
    schedulerState.setLineNumber(Thread.currentThread(), className, methodName, lineNumber);
    yield();
  }

  @Override
  public void beforeLoadClass() {
    //Do nothing
  }

  @Override
  public void afterLoadClass() {
    //Do nothing
  }

  /**
   * Potentially yield the current thread and switch to a new thread
   */
  protected void yield() {
    lock.lock();
    try {
      Thread nextThread = schedulerState.chooseNextThread(Thread.currentThread());
      notify(nextThread);
      waitToBeScheduled();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Park the current thread and pick a new thread to continue.
   */
  @Override
  public void replacePark(
      ExecutionEventListener defaultAction,
      Object blocker) {
    lock.lock();
    try {
      Thread nextThread = schedulerState.park(Thread.currentThread());
      notify(nextThread);
      waitToBeScheduled();
    } finally {
      lock.unlock();
    }
  }

  /**
   * This could park the current thread, but timed waits are currently consider no-ops, since
   * they could eventually wake up.
   */
  @Override
  public void replaceParkNanos(
      ExecutionEventListener defaultAction,
      Object blocker, long timeout) {
    waitForTimeout(timeout);
  }

  /**
   * This could park the current thread, but timed waits are currently consider no-ops, since
   * they could eventually wake up.
   */
  @Override
  public void replaceParkUntil(
      ExecutionEventListener defaultAction,
      Object blocker, long deadline) {
    long timeout = deadline - System.currentTimeMillis() - deadline;
    waitForTimeout(timeout);
  }

  /**
   * Unpark the given thread, and potentially switch to it or another thread.
   */
  @Override
  public void replaceUnpark(ExecutionEventListener defaultAction, final Thread thread) {
    lock.lock();
    try {
      Thread nextThread = schedulerState.unpark(thread);
      notify(nextThread);
      waitToBeScheduled();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Indicate the current thread is about to enter a synchronized block. If the
   * monitor is already held, schedule another thread.
   */
  @Override
  public void beforeSynchronization(final Object sync) {
    yield();
    lock.lock();
    try {
      Thread nextThread = schedulerState.monitorEnter(Thread.currentThread(), sync);
      notify(nextThread);
      waitToBeScheduled();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Indicate the current thread is about to leave a synchronized block. Potentially schedule
   * another thread.
   */
  @Override
  public void afterSynchronization(final Object sync) {
    lock.lock();
    try {
      Thread nextThread = schedulerState.monitorExit(Thread.currentThread(), sync);
      notify(nextThread);
      waitToBeScheduled();
    } finally {
      lock.unlock();
    }

  }

  @Override
  public void replaceWait(
      ExecutionEventListener defaultAction,
      final Object sync, final long timeout, int nanos) {
    monitorControl.monitorExit(sync);
    try {
      if (timeout > 0) {
        waitForTimeout(timeout);

        return;
      }
      lock.lock();
      try {
        Thread nextThread = schedulerState.wait(Thread.currentThread(), sync);
        notify(nextThread);
        waitToBeScheduled();
      } finally {
        lock.unlock();
      }
    } finally {
      monitorControl.monitorEnter(sync);
    }
  }

  private void waitForTimeout(long timeout) {
    //A wait with a timeout of anything greater than 0 is basically just a yield, because
    //we need to test all of the orderings where this thread gives up on the wait
    //before another thread continues. We should probably advance the clock somehow
    //if this thread is not notified.
    //
    yield();
  }

  @Override
  public void replaceJoin(ExecutionEventListener defaultAction, final Thread thread,
                          final long timeout, int nanos) {
    if(timeout > 0) {
      //TODO This should consume a notify, if there is one present
      waitForTimeout(timeout);
      return;
    }
    lock.lock();
    try {
      Thread nextThread = schedulerState.join(Thread.currentThread(), thread);
      notify(nextThread);
      waitToBeScheduled();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void replaceNotify(
      ExecutionEventListener defaultAction,
      final Object sync) {

    lock.lock();
    try {
      schedulerState.notify(Thread.currentThread(), sync);
    } finally {
      lock.unlock();
    }

  }

  @Override
  public void replaceNotifyAll(
      ExecutionEventListener defaultAction,
      final Object sync) {
    lock.lock();
    try {
      schedulerState.notifyAll(Thread.currentThread(), sync);
    } finally {
      lock.unlock();
    }
  }

  /**
   * Notify a thread to be scheduled
   */
  private void notify(Thread scheduledThread) {
    if(scheduledThread == null) {
      return;
    }
    lock.lock();
    try {
      Condition condition = threadConditions.get(scheduledThread);
      condition.signalAll();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Wait to be scheduled in a thread
   */
  private void waitToBeScheduled() {
    lock.lock();
    try {
      Thread currentThread = Thread.currentThread();
      Condition condition = threadConditions.get(currentThread);
      while (!schedulerState.running(currentThread)) {
        condition.awaitUninterruptibly();
      }
    } finally {
      lock.unlock();
    }
  }

  public interface MonitorControl {
    void monitorEnter(Object sync);
    void monitorExit(Object sync);
  }

  public static class DefaultMonitorControl implements MonitorControl {

    @Override
    public void monitorEnter(Object sync) {
      Unsafe.getUnsafe().monitorEnter(sync);
    }

    @Override
    public void monitorExit(Object sync) {
      Unsafe.getUnsafe().monitorExit(sync);
    }
  }
}
