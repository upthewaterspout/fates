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

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Callable;

import com.github.upthewaterspout.fates.core.threading.instrument.asm.instrumented.ClassWithSetName;
import com.github.upthewaterspout.fates.core.threading.instrument.asm.instrumented.ClassWithStart;
import com.github.upthewaterspout.fates.core.threading.instrument.asm.instrumented.ClassWithStartLambda;
import org.junit.Test;

public class InstrumentThreadSynchronizedMethodsTest extends InstrumentationTest {

  @Test
  public void callsToThreadSetNameAreInstrumented() throws Exception {
    String className = ClassWithSetName.class.getCanonicalName();
    Callable<Thread> object = transformAndCreate(className);
    Thread thread = object.call();
    verify(hook, times(1)).beforeSynchronization(eq(thread));
    verify(hook, times(1)).afterSynchronization(eq(thread));
  }

  @Test
  public void callsToThreadStartAreInstrumented() throws Exception {
    String className = ClassWithStart.class.getCanonicalName();
    Callable<Thread> object = transformAndCreate(className);
    Thread thread = object.call();
    verify(hook, times(1)).beforeSynchronization(eq(thread));
    verify(hook, times(1)).afterSynchronization(eq(thread));

    verify(hook, times(1)).beforeThreadStart(eq(thread));
    verify(hook, times(1)).afterThreadStart(eq(thread));
  }

  @Test
  public void callsToThreadStartWithLambdaAreInstrumented() throws Exception {
    String className = ClassWithStartLambda.class.getCanonicalName();
    Callable<Thread> object = transformAndCreate(className);
    Thread thread = object.call();
    verify(hook, times(1)).beforeSynchronization(eq(thread));
    verify(hook, times(1)).afterSynchronization(eq(thread));

    verify(hook, times(1)).beforeThreadStart(eq(thread));
    verify(hook, times(1)).afterThreadStart(eq(thread));
  }

}
