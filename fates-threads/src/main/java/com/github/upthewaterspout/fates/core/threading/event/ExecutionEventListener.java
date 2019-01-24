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

package com.github.upthewaterspout.fates.core.threading.event;

import com.github.upthewaterspout.fates.core.threading.ThreadFates;
import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventSingleton;
import com.github.upthewaterspout.fates.core.threading.scheduler.ThreadSchedulingListener;

/**
 * Listener for receiving events from users instrumented bytecode. This listener is invoked
 * from {@link ExecutionEventSingleton}, which itself is invoked by directly from new bytecode
 * that is added to users classes.
 *
 * In practice, the {@link ThreadFates} uses a chain of {@link ExecutionEventListener}s that
 * start from the {@link ExecutionEventSingleton} and end with a {@link ThreadSchedulingListener}
 */
public interface ExecutionEventListener {
  /**
   * Called in the parent thread before the thread is started.
   * @param thread the thread to start
   */
  void beforeThreadStart(Thread thread);

  /**
   * Called in the parent thread after the child thread has started.
   * @param thread the thread to start
   */
  void afterThreadStart(Thread thread);

  /**
   * Called in a thread that is about to exit
   */
  void beforeThreadExit();

  /**
   * Replaces a thread join call
   * @param defaultAction a listener that can perform the JDKs default join
   * @param thread The thread joined on
   * @param timeout time to wait for the join in milliseconds
   * @param nanos nanoseconds to wait
   * @throws InterruptedException if the current thread is interrupted
   */
  void replaceJoin(ExecutionEventListener defaultAction, Thread thread, long timeout, int nanos) throws InterruptedException;

  /**
   * Park the current thread until some other thread calls unpark. Replaces
   * LockSupport.part()
   * @param defaultAction a listener that can perform the JDKs default behavior
   * @param blocker the blocker to park on
   */
  void replacePark(ExecutionEventListener defaultAction, final Object blocker);

  /**
   * Park the current thread until some other thread calls unpark. Replaces
   * LockSupport.part()
   * @param defaultAction a listener that can perform the JDKs default behavior
   * @param blocker the blocker to park on
   * @param timeout the time to wait
   */
  void replaceParkNanos(ExecutionEventListener defaultAction, Object blocker, long timeout);

  /**
   *  Park the current thread until some other thread calls unpark. Replaces
   *  LockSupport.part()
   * @param defaultAction a listener that can perform the JDKs default behavior
   * @param blocker the blocker to park on
   * @param deadline the time to wait until
   */
  void replaceParkUntil(ExecutionEventListener defaultAction, Object blocker, long deadline);

  /**
   *  Unpark the given thread
   * @param defaultAction a listener that can perform the JDKs default behavior
   * @param thread thread to unpark
   */
  void replaceUnpark(ExecutionEventListener defaultAction, Thread thread);

  /**
   * Wait until there is a notify or notifyAll call for the given object
   * @param defaultAction a listener that can perform the JDKs default behavior
   * @param sync the object to wait on
   * @param timeout timeout in millis
   * @param nanos timeout in seconds
   * @throws InterruptedException if the current thread is interrupted
   */
  void replaceWait(ExecutionEventListener defaultAction, Object sync, long timeout, int nanos) throws InterruptedException;

  /**
   * Wakeup a thread waiting on the given object
   * @param defaultAction a listener that can perform the JDKs default behavior
   * @param sync the object to notify
   */
  void replaceNotify(ExecutionEventListener defaultAction, Object sync);

  /**
   * Wakeup any threads waiting on the given object
   * @param defaultAction a listener that can perform the JDKs default behavior
   * @param sync the object to notify
   */
  void replaceNotifyAll(ExecutionEventListener defaultAction, Object sync);

  /**
   * Called before a synchronized block is entered
   * @param sync the object to sync on
   */
  void beforeSynchronization(Object sync);

  /**
   * Called after a synchronized block is exited
   * @param sync the object to sync on
   */
  void afterSynchronization(Object sync);

  /**
   * Called before a GETFIELD instruction
   * @param owner The object which owns the field
   * @param fieldName
   * @param className  The class the field is on
   * @param methodName The method the get is in
   * @param lineNumber The line number of the get
   */
  void beforeGetField(Object owner, String fieldName, String className, String methodName,
                      int lineNumber);

  /**
   * Called before a SETFIELD instruction
   * @param owner The object which owns the field
   * @param fieldValue the new value of the field, or null if the field is primitive
   * @param fieldName
   * @param className  The class the field is on
   * @param methodName The method the get is in
   * @param lineNumber The line number of the get
   */
  void beforeSetField(Object owner, Object fieldValue, String fieldName, String className,
                      String methodName, int lineNumber);

  /**
   * Called when a method call starts
   * @param className the name of the class
   * @param methodName the name of the method
   */
  void beforeMethod(String className, String methodName);

  /**
   * Called when a method call finishes
   * @param className the name of the class
   * @param methodName the name of the method
   */
  void afterMethod(String className, String methodName);


  /**
   * Called after a new object is created in the current thread
   * (but before the constructor!)
   * @param object - the object being created
   */
  void afterNew(Object object);
}
