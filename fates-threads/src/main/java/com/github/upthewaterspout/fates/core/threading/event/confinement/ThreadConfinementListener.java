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

package com.github.upthewaterspout.fates.core.threading.event.confinement;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import com.github.upthewaterspout.fates.core.threading.event.DelegatingExecutionEventListener;
import com.github.upthewaterspout.fates.core.threading.event.ExecutionEventListener;

/**
 * {@link ExecutionEventListener} that does not pass on events that happen to objects
 * which are reachable only by the current thread.
 *
 * <br><br>
 * Basic algorithm
 * <ul>
 * <li> When an object is created, it is stored in a thread confined set </li>
 * <li> Calls to beforeGet/Set on anything in the thread local set are not passed on </li>
 * <li> If thread confined object added as the value of a field on a non thread confined object,
 *      the object anything reachable by it are removed from the the test </li>
 * </ul>
 *
 * Reachability is determined by transversing references from the object through reflection.
 *
 */

//TODO - also skip these calls if they affect a thread confined object. These are more
// complicated because we still want the side effects of these calls (locking a monitor)
// But we don't necessarily need to consider them events where we might want to schedule
// a different thread.
//  listener.beforeSynchronization("someobject");
//  listener.afterSynchronization("someobject");
//  listener.replaceWait();
//  listener.replaceNotify();
//  listener.replaceNotifyAll();
public class ThreadConfinementListener extends DelegatingExecutionEventListener {
  private ReachableObjectFinder reachableObjects = new ReachableObjectFinder();

  private ThreadLocal<Set<Object>> threadConfinedObjects = ThreadLocal.withInitial(() -> Collections
      .newSetFromMap(new IdentityHashMap<>()));

  public ThreadConfinementListener(
      ExecutionEventListener listener) {
    super(listener);
  }


  @Override
  public void beforeGetField(Object owner, String fieldName, String className,
                             String methodName, int lineNumber) {
    if(threadConfinedObjects.get().contains(owner)) {
      //Do nothing if the object is confined to this thread
      return;
    }
    delegate.beforeGetField(owner, fieldName, className, methodName, lineNumber);
  }

  @Override
  public void beforeSetField(Object owner, Object fieldValue, String fieldName,
                             String className, String methodName,
                             int lineNumber) {
    if(threadConfinedObjects.get().contains(owner)) {
      //Do nothing if the object is confined to this thread
      return;
    }

    removeThreadConfinedObject(fieldValue);
    delegate.beforeSetField(owner, fieldValue, fieldName, className, methodName, lineNumber);
  }

  private void removeThreadConfinedObject(Object fieldValue) {
    Set<Object> threadLocalObjects = threadConfinedObjects.get();
    reachableObjects.stream(fieldValue, threadLocalObjects::contains)
        .forEach(threadLocalObjects::remove);
  }

  @Override
  public void beforeMethod(String className, String methodName) {
    delegate.beforeMethod(className, methodName);
  }

  @Override
  public void afterMethod(String className, String methodName) {
    delegate.afterMethod(className, methodName);
  }

  @Override
  public void afterNew(Object object) {
    threadConfinedObjects.get().add(object);
    delegate.afterNew(object);
  }
}
