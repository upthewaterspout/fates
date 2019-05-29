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

import static java.lang.Thread.currentThread;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Callable;

import com.github.upthewaterspout.fates.core.threading.instrument.agent.FatesMethodEntryExitFilter;
import com.github.upthewaterspout.fates.core.threading.instrument.asm.instrumented.ClassWithPark;
import com.github.upthewaterspout.fates.core.threading.instrument.asm.instrumented.ClassWithParkNanos;
import com.github.upthewaterspout.fates.core.threading.instrument.asm.instrumented.ClassWithParkUntil;
import com.github.upthewaterspout.fates.core.threading.instrument.asm.instrumented.ClassWithUnpark;
import com.github.upthewaterspout.fates.core.threading.instrument.classloader.TransformingClassLoader;
import org.junit.Test;

public class InstrumentLockSupportTest extends InstrumentationTest {
  @Test
  public void callsToParkAreReplaced() throws Exception {
    doNothing().when(hook).replacePark(any(), any());
    AsmTransformer transformer = new AsmTransformer(new FatesMethodEntryExitFilter());
    String className = ClassWithPark.class.getCanonicalName();
    TransformingClassLoader loader = new TransformingClassLoader(transformer, className);
    Class<?> clazz = loader.loadClass(className);
    Callable object = (Callable) clazz.newInstance();
    object.call();
    verify(hook, times(1)).replacePark(any(), eq(null));
  }

  @Test
  public void callsToUnparkAreReplaced() throws Exception {
    String className = ClassWithUnpark.class.getCanonicalName();
    Callable object = transformAndCreate(className);
    object.call();
    verify(hook, times(1)).replaceUnpark(any(), eq(currentThread()));
  }

  @Test
  public void callsToParkNanosAreReplaced() throws Exception {
    doNothing().when(hook).replaceParkNanos(any(), any(), anyLong());
    String className = ClassWithParkNanos.class.getCanonicalName();
    Callable object = transformAndCreate(className);
    object.call();
    verify(hook, times(1)).replaceParkNanos(any(), eq(null), eq(100L));
  }

  @Test
  public void callsToParkUntilAreReplaced() throws Exception {
    doNothing().when(hook).replaceParkUntil(any(), any(), anyLong());
    String className = ClassWithParkUntil.class.getCanonicalName();
    Callable object = transformAndCreate(className);
    object.call();
    verify(hook, times(1)).replaceParkUntil(any(), eq(null), eq(5L));
  }

}