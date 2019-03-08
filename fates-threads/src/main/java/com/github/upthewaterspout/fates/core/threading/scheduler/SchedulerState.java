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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Keeps track of all of the state related to thread scheduling - which threads
 * are under test, which threads are blocked, etc.
 *
 * This is really the main scheduling logic, which wrapped in {@link ThreadSchedulingListener}
 * which manages thread safety for this state. All of the state here should be accessed through
 * {@link ThreadSchedulingListener}
 */
class SchedulerState {

  private final Decider decider;
  final ThreadMapping threadMapping = new ThreadMapping();
  final ThreadState threadState = new ThreadState();
  private final SynchronizationTracker<Thread> synchronizationTracker = new SynchronizationTracker<>();
  private final JoinTracker<Thread> joinTracker = new JoinTracker<>();
  private final Set<Thread> interruptedThreads = new HashSet<>();


  public void newThread(Thread thread, Thread parent) {
    threadMapping.newThread(thread, parent);
    threadState.newThread(thread);
  }

  /**
   * Last visited line number
   */
  private LineNumber lastLineNumber = new LineNumber("", "java.lang.Thread", "run", 0);

  public SchedulerState(Decider decider) {
    this.decider = decider;
  }

  /**
   * Ask the state space to choose the next thread to run
   */
  public Thread chooseNextThread(Thread thread) {
    verifyThread(thread);
    threadState.unblock(thread);

    return getNextThread();
  }


  public boolean running(Thread thread) {
    return threadState.isRunning(thread);
  }


  public Thread park(final Thread thread) {
    verifyThread(thread);
    threadState.block(thread);
    return getNextThread();
  }

  public Thread unpark(final Thread thread) {
    verifyThread(thread);
    threadState.unblock(thread);
    return getNextThread();
  }

  public Thread interrupt(final Thread thread) {
    verifyThread(thread);
    threadInterrupted(thread);
    return getNextThread();
  }

  private void threadInterrupted(Thread thread) {
    verifyThread(thread);
    interruptedThreads.add(thread);
    joinTracker.interrupt(thread);
    if(!synchronizationTracker.interrupt(thread)) {
      threadState.unblock(thread);
    }
  }

  public Thread threadTerminated(Thread thread) {
    verifyThread(thread);
    Collection<Thread> unblockedThreads = joinTracker.threadTerminated(thread);
    threadState.unblock(unblockedThreads);
    threadState.terminate(thread);
    interruptedThreads.remove(thread);
    return getNextThread();
  }

  private Thread getNextThread() {
    if(threadState.hasRunningThread()) {
      //If there is already a thread running, let it continue
      //without scheduling a new thread
      return null;
    }

    threadState.checkForUnscheduledThread();

    ThreadID scheduledThreadID = decider.decide(lastLineNumber, threadState.getUnscheduledThreads().map(threadMapping::getThreadID).collect(
        Collectors.toSet()));
    Thread scheduledThread = threadMapping.getThread(scheduledThreadID);
    threadState.resume(scheduledThread);
    Collection<Thread> blockedThreads = synchronizationTracker.threadResumed(scheduledThread);
    threadState.block(blockedThreads);

    return scheduledThread;
  }

  public Thread monitorEnter(Thread thread, final Object sync) {
    verifyThread(thread);
    Collection<Thread> threadsToBlock = synchronizationTracker.monitorEnter(thread, sync);
    threadState.block(threadsToBlock);
    return getNextThread();
  }

  public Thread monitorExit(Thread thread, final Object sync) {
    verifyThread(thread);
    Collection<Thread> unblockedThreads = synchronizationTracker.monitorExit(thread, sync);
    threadState.unblock(unblockedThreads);
    return chooseNextThread(thread);
  }

  public Thread wait(Thread thread, final Object sync) {
    verifyThread(thread);
    if(isInterrupted(thread, false)) {
      return thread;
    }
    Collection<Thread> unblockedThreads = synchronizationTracker.wait(thread, sync);
    threadState.block(thread);
    threadState.unblock(unblockedThreads);
    return getNextThread();
  }

  public void notify(Thread thread, final Object sync) {
    verifyThread(thread);
    synchronizationTracker.notify(thread, sync);

  }

  public void notifyAll(Thread thread, final Object sync) {
    verifyThread(thread);
    synchronizationTracker.notifyAll(thread, sync);
  }

  public Thread join(Thread joiner, Thread joinee) {
    verifyThread(joiner);
    verifyThread(joinee);
    if(!threadState.hasThread(joinee)) {
      return joiner;
    }
    if(isInterrupted(joiner, false)) {
      return joiner;
    }

    threadState.block(joiner);
    joinTracker.join(joiner, joinee);
    return getNextThread();
  }

  public void setLineNumber(Thread currentThread, String className, String methodName,
                            int lineNumber) {
    this.lastLineNumber = new LineNumber(currentThread.getName(), className, methodName, lineNumber);
  }

  public boolean isInterrupted(Thread thread, boolean clearInterrupt) {
    verifyThread(thread);
    if(clearInterrupt) {
      return interruptedThreads.remove(thread);
    } else {
      return interruptedThreads.contains(thread);
    }
  }

  private void verifyThread(Thread thread) {
    if(!threadMapping.hasThread(thread)) {
      throw new IllegalStateException("Scheduler was asked to handle untracked thread " + thread);
    }

  }
}