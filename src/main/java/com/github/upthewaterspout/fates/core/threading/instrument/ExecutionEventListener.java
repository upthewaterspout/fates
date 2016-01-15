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

import com.github.upthewaterspout.fates.core.threading.Fates;
import com.github.upthewaterspout.fates.core.threading.scheduler.ThreadSchedulingListener;

/**
 * Listener for receiving events from users instrumented bytecode. This listener is invoked
 * from {@link ExecutionEventSingleton}, which itself is invoked by directly from new bytecode
 * that is added to users classes.
 *
 * In practice, the {@link Fates} uses a chain of {@link ExecutionEventListener}s that
 * start from the {@link ExecutionEventSingleton} and end with a {@link ThreadSchedulingListener}
 */
public interface ExecutionEventListener {
  /**
   * Called in the parent thread before the thread is started.
   */
  void beforeThreadStart(Thread thread);

  /**
   * Called in the parent thread after the child thread has started.
   */
  void afterThreadStart(Thread thread);

  /**
   * Called in a thread that is about to exit
   */
  void beforeThreadExit();

  /**
   * Replaces a thread join call
   */
  void replaceJoin(ExecutionEventListener defaultAction, Thread thread, long timeout, int nanos) throws InterruptedException;

  /**
   *  Park the current thread until some other thread calls unpark. Replaces
   *  LockSupport.part()
   */
  void replacePark(ExecutionEventListener defaultAction, final Object blocker);

  /**
   *  Park the current thread until some other thread calls unpark. Replaces
   *  LockSupport.part()
   */
  void replaceParkNanos(ExecutionEventListener defaultAction, Object blocker, long timeout);

  /**
   *  Park the current thread until some other thread calls unpark. Replaces
   *  LockSupport.part()
   */
  void replaceParkUntil(ExecutionEventListener defaultAction, Object blocker, long deadline);

  /**
   *  Unpark the given thread
   */
  void replaceUnpark(ExecutionEventListener defaultAction, Thread thread);

  /**
   * Wait until there is a notify or notifyAll call for the given object
   */
  void replaceWait(ExecutionEventListener defaultAction, Object sync, long timeout, int nanos) throws InterruptedException;

  /**
   * Wakeup a thread waiting on the given object
   */
  void replaceNotify(ExecutionEventListener defaultAction, Object sync);

  /**
   * Wakeup any threads waiting on the given object
   */
  void replaceNotifyAll(ExecutionEventListener defaultAction, Object sync);

  /**
   * Called before a synchronized block is entered
   */
  void beforeSynchronization(Object sync);

  /**
   * Called after a synchronized block is exited
   * @param sync
   */
  void afterSynchronization(Object sync);

  /**
   * Called before a GETFIELD instruction
   */
  void beforeGetField(String className, String methodName, int lineNumber);

  /**
   * Called before a SETFIELD instruction
   */
  void beforeSetField(String className, String methodName, int lineNumber);


}
