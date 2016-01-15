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

package com.github.upthewaterspout.fates.core.threading.instrument.asm;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Callable;

import com.github.upthewaterspout.fates.core.threading.instrument.asm.instrumented.ClassWithNanoTimedWaitNotify;
import com.github.upthewaterspout.fates.core.threading.instrument.asm.instrumented.ClassWithTimedWaitNotify;
import com.github.upthewaterspout.fates.core.threading.instrument.asm.instrumented.ClassWithWaitNotify;
import org.junit.Test;

public class InstrumentWaitNotifyTest extends InstrumentationTest {
  @Test
  public void waitNotifyAddsHook() throws Exception {
    String className = ClassWithWaitNotify.class.getCanonicalName();
    Callable object = transformAndCreate(className);
    Object sync = object.call();
    verify(hook, times(1)).replaceWait(any(), eq(sync), eq(0L), eq(0));
    verify(hook, times(1)).replaceNotify(any(), eq(sync));
  }

  @Test
  public void timedWaitNotifyAddsHook() throws Exception {
    String className = ClassWithTimedWaitNotify.class.getCanonicalName();
    Callable object = transformAndCreate(className);
    Object sync = object.call();
    verify(hook, times(1)).replaceWait(any(), eq(sync), eq(100000L), eq(0));
    verify(hook, times(1)).replaceNotify(any(), eq(sync));
  }

  @Test
  public void nanoTimedWaitNotifyAddsHook() throws Exception {
    String className = ClassWithNanoTimedWaitNotify.class.getCanonicalName();
    Callable object = transformAndCreate(className);
    Object sync = object.call();
    verify(hook, times(1)).replaceWait(any(), eq(sync), eq(100000L), eq(10));
    verify(hook, times(1)).replaceNotify(any(), eq(sync));
  }
}