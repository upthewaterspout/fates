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

import com.github.upthewaterspout.fates.core.threading.instrument.asm.instrumented.ClassWithInterrupt;
import com.github.upthewaterspout.fates.core.threading.instrument.asm.instrumented.ClassWithInterrupted;
import com.github.upthewaterspout.fates.core.threading.instrument.asm.instrumented.ClassWithIsInterrupted;
import org.junit.Test;

public class InstrumentThreadInterruptTest extends InstrumentationTest {
  @Test
  public void callsToJoinAreReplaced() throws Exception {
    String className = ClassWithInterrupt.class.getCanonicalName();
    Callable<Thread> object = transformAndCreate(className);
    Thread interrupted = object.call();
    verify(hook, times(1)).replaceInterrupt(any(), eq(interrupted));
  }

  @Test
  public void callsToIsInterruptedAreReplaced() throws Exception {
    String className = ClassWithIsInterrupted.class.getCanonicalName();
    Callable<Thread> object = transformAndCreate(className);
    Thread checked = object.call();
    verify(hook, times(1)).replaceIsInterrupted(any(), eq(checked), eq(false));
  }

  @Test
  public void callsToInterruptedAreReplaced() throws Exception {
    String className = ClassWithInterrupted.class.getCanonicalName();
    Callable<Thread> object = transformAndCreate(className);
    Thread checked = object.call();
    verify(hook, times(1)).replaceIsInterrupted(any(), eq(checked), eq(true));
  }

}