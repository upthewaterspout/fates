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

package com.github.upthewaterspout.fates.core.threading.harness;

import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventListener;

/**
 * A decorator for a {@link ExecutionEventListener} that allows disabling the {@link
 * ExecutionEventListener#beforeGetField(Object, String, String, int)}  and {@link
 * ExecutionEventListener#beforeSetField(Object, Object, String, String, int)} while class loading is happening.
 *
 * This decorator is used to suppress context switching that would normally happen from field
 * accesses within classloading, to reduce the number of decision points in a test.
 */
public class AtomicClassLoadingDecorator extends DelegatingExecutionEventListener {

  private ThreadLocal<EntryCount> atomicEntryCount = new ThreadLocal<EntryCount> () {
    @Override protected EntryCount initialValue() {
      return new EntryCount();
    }
  };

  public AtomicClassLoadingDecorator(final ExecutionEventListener delegate) {
    super(delegate);
  }

  @Override
  protected boolean beforeEvent() {
    return atomicEntryCount.get().isZero();
  }

  @Override
  public void beforeLoadClass() {
    beginAtomic();
    super.beforeLoadClass();
  }

  @Override
  public void afterLoadClass() {
    super.afterLoadClass();
    endAtomic();
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
