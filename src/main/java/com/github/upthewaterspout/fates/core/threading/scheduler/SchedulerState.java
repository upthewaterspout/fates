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

import com.github.upthewaterspout.fates.core.states.Decider;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Keeps track of all of the state related to thread scheduling - which threads
 * are under test, which threads are blocked, etc.
 *
 * This is really the main scheduling logic, which wrapped in {@link ThreadSchedulingListener}
 * which manages thread safety for this state. All of the state here should be accessed through
 * {@link ThreadSchedulingListener}
 */
class SchedulerState {
  private Decider decider;
  /**
   * All threads in this run
   */
  private final Map<Thread, ThreadID> threadtoID = new HashMap<>();

  /**
   * All threads in this run
   */
  private final Map<ThreadID, Thread> idToThread = new HashMap<>();
  /**
   * The set of threads that are currently parked in a yield operation
   */
  private final Set<ThreadID> unscheduledThreads = new HashSet<>();
  /**
   * The currently running thread
   */
  private final Set<Thread> runningThreads = new HashSet<>();

  /**
   * Threads that currently blocked waiting for a monitor, lock, or notification
   */
  private final Set<Thread> blockedThreads = new HashSet<>();

  private SynchronizationTracker synchronizationTracker = new SynchronizationTracker();

  private JoinTracker joinTracker = new JoinTracker();

  /**
   * Last visited line number
   */
  private LineNumber lastLineNumber = new LineNumber("", "java.lang.Thread", "run", 0);

  public SchedulerState(Decider decider) {
    this.decider = decider;
  }

  void unschedule(Thread thread) {
    ThreadID myThreadId = threadtoID.get(thread);
    unscheduledThreads.add(myThreadId);
    blockedThreads.remove(thread);
    runningThreads.remove(thread);
  }

  public void newThread(Thread thread, Thread parent) {
    ThreadID threadID = ThreadID.create(thread, threadtoID.get(parent));
    threadtoID.put(thread, threadID);
    idToThread.put(threadID, thread);
    runningThreads.add(thread);
  }

  /**
   * Ask the state space to choose the next thread to run
   */
  public Thread chooseNextThread(Thread currentThread) {
    unschedule(currentThread);

    return getNextThread();
  }

  public boolean running(Thread thread) {
    return runningThreads.contains(thread);
  }

  public Thread park(final Thread thread) {
    threadBlocked(thread);
    return getNextThread();
  }

  private void threadBlocked(final Thread thread) {
    ThreadID threadId = threadtoID.get(thread);
    runningThreads.remove(thread);
    unscheduledThreads.remove(threadId);
    blockedThreads.add(thread);
  }

  public Thread unpark(final Thread thread) {
    threadUnblocked(thread);
    return getNextThread();

  }

  private void threadUnblocked(final Thread thread) {
    ThreadID threadId = threadtoID.get(thread);
    blockedThreads.remove(thread);
    runningThreads.remove(thread);
    unscheduledThreads.add(threadId);
  }

  public Thread threadTerminated(Thread thread) {
    ThreadID threadId = threadtoID.remove(thread);
    idToThread.remove(threadId);
    unscheduledThreads.remove(threadId);
    blockedThreads.remove(thread);
    runningThreads.remove(thread);
    Collection<Thread> unblockedThreads = joinTracker.threadTerminated(thread);

    unblockedThreads.stream().forEach(this::threadUnblocked);
    return getNextThread();
  }

  private Thread getNextThread() {
    if(!runningThreads.isEmpty()) {
      //If there is already a thread running, let it continue
      //without scheduling a new thread
      return null;
    }

    if(unscheduledThreads.isEmpty()) {
      blockedThreads.stream().forEach(thread -> {
        System.out.println(thread.getName());
        Arrays.stream(thread.getStackTrace()).forEach(element ->
        System.out.println("  at " + element));
      });
      throw new IllegalStateException("Unscheduled threads is empty. blockedThreads="
        + blockedThreads +", runningThreads="
        + runningThreads + ", currentThread="
        + Thread.currentThread().getName());
    }

    ThreadID scheduledThreadID = decider.decide(lastLineNumber, unscheduledThreads);
    Thread scheduledThread = idToThread.get(scheduledThreadID);
    runningThreads.add(scheduledThread);
    unscheduledThreads.remove(scheduledThreadID);
    Collection<Thread> blockedThreads = synchronizationTracker.threadResumed(scheduledThread);
    markBlocked(blockedThreads);

    return scheduledThread;
  }

  private void markBlocked(final Collection<Thread> blockedThreads) {
    blockedThreads.stream().forEach(this::threadBlocked);
  }

  public Thread monitorEnter(Thread thread, final Object sync) {
    Collection<Thread> threadsToBlock = synchronizationTracker.monitorEnter(thread, sync);
    markBlocked(threadsToBlock);
    return getNextThread();
  }

  public Thread monitorExit(Thread thread, final Object sync) {
    Collection<Thread> unblockedThreads = synchronizationTracker.monitorExit(thread, sync);
    unblockedThreads.stream().forEach(this::threadUnblocked);
    return chooseNextThread(thread);
  }

  public Thread wait(Thread thread, final Object sync) {
    Collection<Thread> unblockedThreads = synchronizationTracker.wait(thread, sync);
    threadBlocked(thread);
    unblockedThreads.stream().forEach(this::threadUnblocked);
    return getNextThread();
  }

  public void notify(Thread thread, final Object sync) {
    synchronizationTracker.notify(thread, sync);

  }

  public void notifyAll(Thread thread, final Object sync) {
    synchronizationTracker.notifyAll(thread, sync);
  }

  public Thread join(Thread joiner, Thread joinee) {
    if(!threadtoID.containsKey(joinee)) {
      return joiner;
    }

    threadBlocked(joiner);
    joinTracker.join(joiner, joinee);
    return getNextThread();
  }

  public void setLineNumber(Thread currentThread, String className, String methodName,
                            int lineNumber) {
    this.lastLineNumber = new LineNumber(currentThread.getName(), className, methodName, lineNumber);
  }

  private static class LineNumber {
    private final String currentThread;
    private final String className;
    private final String methodName;
    private final int lineNumber;

    public LineNumber(String currentThread, String className, String methodName,
                      int lineNumber) {
      this.currentThread = currentThread;
      this.className = className;
      this.methodName = methodName;
      this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
      return  className + "." + methodName + "(" + getShortClassName()
          + ".java:" + lineNumber + ")(" + currentThread + ")";
    }

    private String getShortClassName() {
      if (className.lastIndexOf(".") == -1) {
        return className;
      } else {
        return className.substring(className.lastIndexOf(".") + 1, className.length());
      }
    }
  }
}