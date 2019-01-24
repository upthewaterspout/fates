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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import com.github.upthewaterspout.fates.core.threading.harness.AtomicClassLoadingDecorator;
import com.github.upthewaterspout.fates.core.threading.harness.AtomicMethodListener;
import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventListener;
import org.junit.Test;

public class AtomicMethodListenerTest {

  public static final String CLASS_NAME = AtomicMethodListenerTest.class.getCanonicalName();
  public ExecutionEventListener delegate = mock(ExecutionEventListener.class);
  public AtomicMethodListener scheduler = new AtomicMethodListener(delegate, Collections.singleton(AtomicMethodListenerTest.class));

  @Test
  public void noAtomicControlShouldAllowBeforeGetFieldCall() {
    scheduler.beforeGetField("owner", CLASS_NAME, "method", 5);
    verify(delegate).beforeGetField("owner", CLASS_NAME, "method", 5);
  }

  @Test
  public void beforeMethodShouldPreventBeforeGetFieldCall() {
    scheduler.beforeMethod(CLASS_NAME, "someMethod");
    scheduler.beforeGetField("owner", CLASS_NAME, "method", 5);
    verify(delegate, times(0)).beforeGetField("owner", CLASS_NAME, "method", 5);
  }

  @Test
  public void afterMethodShouldAllowBeforeGetFieldCall() {
    scheduler.beforeMethod(CLASS_NAME, "loadClass");
    scheduler.afterMethod(CLASS_NAME, "loadClass");
    scheduler.beforeGetField("owner", CLASS_NAME, "method", 5);
    verify(delegate).beforeGetField("owner", CLASS_NAME, "method", 5);
  }

  @Test
  public void unbalancedBeginEndShouldPreventBeforeGetField() {
    scheduler.beforeMethod(CLASS_NAME, "loadClass");
    scheduler.beforeMethod(CLASS_NAME, "loadClass");
    scheduler.afterMethod(CLASS_NAME, "loadClass");
    scheduler.beforeGetField("owner", CLASS_NAME, "method", 5);
    verify(delegate, times(0)).beforeGetField("owner", CLASS_NAME, "method", 5);
  }

  @Test
  public void balancedBeginEndShouldAllowBeforeGetField() {
    scheduler.beforeMethod(CLASS_NAME, "loadClass");
    scheduler.beforeMethod(CLASS_NAME, "loadClass");
    scheduler.afterMethod(CLASS_NAME, "loadClass");
    scheduler.afterMethod(CLASS_NAME, "loadClass");
    scheduler.beforeGetField("owner", CLASS_NAME, "method", 5);
    verify(delegate).beforeGetField("owner", CLASS_NAME, "method", 5);
  }
}