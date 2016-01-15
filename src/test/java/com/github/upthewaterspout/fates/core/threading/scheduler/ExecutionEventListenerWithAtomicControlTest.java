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

import static org.mockito.Mockito.*;

import com.github.upthewaterspout.fates.core.threading.harness.ExecutionEventListenerWithAtomicControl;
import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventListener;
import org.junit.Test;

public class ExecutionEventListenerWithAtomicControlTest {

  public ExecutionEventListener delegate = mock(ExecutionEventListener.class);
  public ExecutionEventListenerWithAtomicControl
      scheduler = new ExecutionEventListenerWithAtomicControl(delegate);

  @Test
  public void noAtomicControlShouldAllowBeforeGetFieldCall() {
    scheduler.beforeGetField("class", "method", 5);
    verify(delegate).beforeGetField("class", "method", 5);
  }

  @Test
  public void beginAtomicShouldPreventBeforeGetFieldCall() {
    scheduler.beginAtomic();
    scheduler.beforeGetField("class", "method", 5);
    verify(delegate, times(0)).beforeGetField("class", "method", 5);
  }

  @Test
  public void endAtomicShouldAllowBeforeGetFieldCall() {
    scheduler.beginAtomic();
    scheduler.endAtomic();
    scheduler.beforeGetField("class", "method", 5);
    verify(delegate).beforeGetField("class", "method", 5);
  }

  @Test
  public void unbalancedBeginEndShouldPreventBeforeGetField() {
    scheduler.beginAtomic();
    scheduler.beginAtomic();
    scheduler.endAtomic();
    scheduler.beforeGetField("class", "method", 5);
    verify(delegate, times(0)).beforeGetField("class", "method", 5);
  }

  @Test
  public void balancedBeginEndShouldAllowBeforeGetField() {
    scheduler.beginAtomic();
    scheduler.beginAtomic();
    scheduler.endAtomic();
    scheduler.endAtomic();
    scheduler.beforeGetField("class", "method", 5);
    verify(delegate).beforeGetField("class", "method", 5);
  }
}