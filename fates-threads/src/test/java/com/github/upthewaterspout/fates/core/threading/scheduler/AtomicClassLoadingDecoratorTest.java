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

import com.github.upthewaterspout.fates.core.threading.harness.AtomicClassLoadingDecorator;
import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventListener;
import org.junit.Test;

public class AtomicClassLoadingDecoratorTest {

  public ExecutionEventListener delegate = mock(ExecutionEventListener.class);
  public AtomicClassLoadingDecorator
      scheduler = new AtomicClassLoadingDecorator(delegate);

  @Test
  public void noAtomicControlShouldAllowBeforeGetFieldCall() {
    scheduler.beforeGetField("owner", "class", "method", 5);
    verify(delegate).beforeGetField("owner", "class", "method", 5);
  }

  @Test
  public void beforeLoadClassShouldPreventBeforeGetFieldCall() {
    scheduler.beforeLoadClass();
    scheduler.beforeGetField("owner", "class", "method", 5);
    verify(delegate, times(0)).beforeGetField("owner", "class", "method", 5);
  }

  @Test
  public void afterLoadClassShouldAllowBeforeGetFieldCall() {
    scheduler.beforeLoadClass();
    scheduler.afterLoadClass();
    scheduler.beforeGetField("owner", "class", "method", 5);
    verify(delegate).beforeGetField("owner", "class", "method", 5);
  }

  @Test
  public void unbalancedBeginEndShouldPreventBeforeGetField() {
    scheduler.beforeLoadClass();
    scheduler.beforeLoadClass();
    scheduler.afterLoadClass();
    scheduler.beforeGetField("owner", "class", "method", 5);
    verify(delegate, times(0)).beforeGetField("owner", "class", "method", 5);
  }

  @Test
  public void balancedBeginEndShouldAllowBeforeGetField() {
    scheduler.beforeLoadClass();
    scheduler.beforeLoadClass();
    scheduler.afterLoadClass();
    scheduler.afterLoadClass();
    scheduler.beforeGetField("owner", "class", "method", 5);
    verify(delegate).beforeGetField("owner", "class", "method", 5);
  }
}