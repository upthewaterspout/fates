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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages of the state of threads for the {@link SchedulerState} class. This class implements
 * a simplified model of threads - a thread that is tracked by this class is is either running,
 * unscheduled, or blocked.
 *
 * Additional state about the thread (like what monitor it is blocked on) is managed by other
 * classes such as {@link SynchronizationTracker} as another layer built on top of this.
 */
class ThreadState {

  /**
   * The set of threads that are currently parked in a yield operation
   */
  private final Set<Thread> unscheduledThreads = new HashSet<>();
  /**
   * The currently running thread
   */
  private final Set<Thread> runningThreads = new HashSet<>();
  /**
   * Threads that currently blocked waiting for a monitor, lock, or notification
   */
  private final Set<Thread> blockedThreads = new HashSet<>();

  ThreadState() {
  }


  void newThread(Thread thread) {
    runningThreads.add(thread);
  }

  boolean running(Thread thread) {
    return runningThreads.contains(thread);
  }

  void block(final Thread thread) {
    runningThreads.remove(thread);
    unscheduledThreads.remove(thread);
    blockedThreads.add(thread);
  }

  void unblock(final Thread thread) {
    blockedThreads.remove(thread);
    runningThreads.remove(thread);
    unscheduledThreads.add(thread);
  }

  void block(final Collection<Thread> blockedThreads) {
    blockedThreads.stream().forEach(this::block);
  }

  Set<Thread> getUnscheduledThreads() {
    return Collections.unmodifiableSet(unscheduledThreads);
  }

  Set<Thread> getBlockedThreads() {
    return Collections.unmodifiableSet(blockedThreads);
  }

  void threadTerminated(Thread thread) {
    unscheduledThreads.remove(thread);
    blockedThreads.remove(thread);
    runningThreads.remove(thread);
  }

  boolean hasRunningThread() {
    return !runningThreads.isEmpty();
  }

  /**
   * Check to make sure there is at least one unscheduled thread
   * @throws IllegalStateException if there are no unscheduled theads.
   */
  void checkForUnscheduledThread() {
    if(unscheduledThreads.isEmpty()) {
      blockedThreads.stream().forEach(thread -> {
        System.out.println(thread.getName());
        Arrays.stream(thread.getStackTrace()).forEach(element ->
            System.out.println("  at " + element));
      });
      throw new IllegalStateException("Deadlock detected, all threads are are blocked. blockedThreads="
        + blockedThreads +", runningThreads="
        + runningThreads + ", currentThread="
        + Thread.currentThread().getName());
    }
  }

  void threadResumed(Thread scheduledThread) {
    runningThreads.add(scheduledThread);
    unscheduledThreads.remove(scheduledThread);
  }

  void unblock(Collection<Thread> unblockedThreads) {
    unblockedThreads.stream().forEach(this::unblock);
  }
}