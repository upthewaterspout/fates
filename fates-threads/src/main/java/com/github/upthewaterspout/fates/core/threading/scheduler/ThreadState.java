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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Manages of the state of threads for the {@link SchedulerState} class. This class implements
 * a simplified model of threads - a thread that is tracked by this class is is either running,
 * unscheduled, or blocked.
 *
 * Additional state about the thread (like what monitor it is blocked on) is managed by other
 * classes such as {@link SynchronizationTracker} as another layer built on top of this.
 */
class ThreadState {
  private final Map<Thread,State> threadStates = new HashMap<>();

  void newThread(Thread thread) {
    threadStates.put(thread, State.RUNNING);
  }

  void block(final Thread thread) {
    setState(thread, State.BLOCKED);
  }

  void unblock(final Thread thread) {
    setState(thread, State.UNSCHEDULED);
  }

  void resume(Thread thread) {
    setState(thread, State.RUNNING);
  }

  private void setState(Thread thread, State state) {
    if(threadStates.containsKey(thread)) {
      threadStates.put(thread, state);
    }
  }

  void terminate(Thread thread) {
    threadStates.remove(thread);
  }

  Stream<Thread> getUnscheduledThreads() {
    return getThreadsInState(State.UNSCHEDULED);
  }

  private Stream<Thread> getThreadsInState(State state) {
    return threadStates.entrySet().stream()
        .filter(entry -> entry.getValue().equals(state))
        .map(Map.Entry::getKey);
  }


  boolean hasRunningThread() {
    return getThreadsInState(State.RUNNING).findAny().isPresent();
  }

  /**
   * Check to make sure there is at least one unscheduled thread
   * @throws IllegalStateException if there are no unscheduled theads.
   */
  void checkForUnscheduledThread() {
    if(!getUnscheduledThreads().findAny().isPresent()) {

      StringBuilder builder = new StringBuilder();
      builder.append("Deadlock detected, all threads are blocked. Thread dumps: \n");
      builder.append("------------------------------------------------------------\n");
      getThreadsInState(State.BLOCKED).forEach(thread -> {
        builder.append(thread.getName()).append("\n");
        Arrays.stream(thread.getStackTrace()).forEach(element ->
            builder.append("  at " + element + "\n"));
        builder.append("------------------------------------------------------------\n");
      });
      throw new IllegalStateException(builder.toString());
    }
  }

  void block(final Collection<Thread> blockedThreads) {
    blockedThreads.stream().forEach(this::block);
  }

  void unblock(Collection<Thread> unblockedThreads) {
    unblockedThreads.stream().forEach(this::unblock);
  }

  boolean isRunning(Thread thread) {
    return State.RUNNING.equals(threadStates.get(thread));
  }

  public boolean isBlocked(Thread thread) {
    return State.BLOCKED.equals(threadStates.get(thread));
  }

  public boolean isUnscheduled(Thread thread) {
    return State.UNSCHEDULED.equals(threadStates.get(thread));
  }

  public boolean hasThread(Thread thread) {
    return threadStates.containsKey(thread);
  }

  public static enum State {
    RUNNING, //Thread that is currently actively running
    BLOCKED, //Thread that is currently blocked
    UNSCHEDULED //Thread that is not blocked, but not currently running

  }
}