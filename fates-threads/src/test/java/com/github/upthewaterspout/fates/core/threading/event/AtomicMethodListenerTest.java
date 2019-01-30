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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class AtomicMethodListenerTest {

  public static final String CLASS_NAME = AtomicMethodListenerTest.class.getCanonicalName();
  public ExecutionEventListener delegate = mock(ExecutionEventListener.class);
  public AtomicMethodListener scheduler = new AtomicMethodListener(delegate,
      Arrays.asList(AtomicMethodListenerTest.class.getName(), AtomicMethodListenerTest.InnerClass.class.getName()));

  @Test
  public void noAtomicControlShouldAllowBeforeGetFieldCall() {
    scheduler.beforeGetField("owner", "any", CLASS_NAME, "method", 5);
    verify(delegate).beforeGetField("owner", "any", CLASS_NAME, "method", 5);
  }

  @Test
  public void beforeMethodShouldPreventBeforeGetFieldCall() {
    scheduler.beforeMethod(CLASS_NAME, "someMethod");
    scheduler.beforeGetField("owner", "any", CLASS_NAME, "method", 5);
    verify(delegate, times(0)).beforeGetField("owner", "any", CLASS_NAME, "method", 5);
  }

  @Test
  public void afterMethodShouldAllowBeforeGetFieldCall() {
    scheduler.beforeMethod(CLASS_NAME, "loadClass");
    scheduler.afterMethod(CLASS_NAME, "loadClass");
    scheduler.beforeGetField("owner", "any", CLASS_NAME, "method", 5);
    verify(delegate).beforeGetField("owner", "any", CLASS_NAME, "method", 5);
  }

  @Test
  public void unbalancedBeginEndShouldPreventBeforeGetField() {
    scheduler.beforeMethod(CLASS_NAME, "loadClass");
    scheduler.beforeMethod(CLASS_NAME, "loadClass");
    scheduler.afterMethod(CLASS_NAME, "loadClass");
    scheduler.beforeGetField("owner", "any", CLASS_NAME, "method", 5);
    verify(delegate, times(0)).beforeGetField("owner", "any", CLASS_NAME, "method", 5);
  }

  @Test
  public void balancedBeginEndShouldAllowBeforeGetField() {
    scheduler.beforeMethod(CLASS_NAME, "loadClass");
    scheduler.beforeMethod(CLASS_NAME, "loadClass");
    scheduler.afterMethod(CLASS_NAME, "loadClass");
    scheduler.afterMethod(CLASS_NAME, "loadClass");
    scheduler.beforeGetField("owner", "any", CLASS_NAME, "method", 5);
    verify(delegate).beforeGetField("owner", "any", CLASS_NAME, "method", 5);
  }

  @Test
  public void beforeMethodShouldPreventBeforeGetFieldCallForNamedInnerClass() {
    scheduler.beforeMethod(InnerClass.class.getName(), "someMethod");
    scheduler.beforeGetField("owner", "any", InnerClass.class.getName(), "method", 5);
    verify(delegate, times(0)).beforeGetField("owner", "any",InnerClass.class.getName() , "method", 5);
  }

  public static class InnerClass {

  }
}