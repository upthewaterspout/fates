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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A decorator for a {@link ExecutionEventListener} that allows disabling execution events
 * while inside a certain list of classes.
 *
 */
public class AtomicMethodListener extends DelegatingExecutionEventListener {

  private final Set<String> suppressedClasses;
  private ThreadLocal<EntryCount> atomicEntryCount = ThreadLocal.withInitial(EntryCount::new);

  public AtomicMethodListener(final ExecutionEventListener delegate, Collection<Class<?>> suppressedClasses) {
    super(delegate);

    this.suppressedClasses = suppressedClasses.stream()
      .map(Class::getName)
        .collect(Collectors.toCollection(HashSet::new));
  }

  @Override
  protected boolean beforeEvent() {
    return atomicEntryCount.get().isZero();
  }

  @Override
  public void beforeMethod(String className, String methodName) {
    if(suppressedClasses.contains(className)) {
      beginAtomic();
    }
    super.beforeMethod(className, methodName);
  }

  @Override
  public void afterMethod(String className, String methodName) {
    super.afterMethod(className, methodName);
    if(suppressedClasses.contains(className)) {
      endAtomic();
    }
  }

  private void beginAtomic() {
    atomicEntryCount.get().increment();
  }

  private void endAtomic() {
    atomicEntryCount.get().decrement();
  }


  private static class EntryCount {
    private int count;

    public void increment() {
      count++;
    }

    public void decrement() {
      count--;
    }

    public boolean isZero() {
      return count <= 0;
    }
  }
}
