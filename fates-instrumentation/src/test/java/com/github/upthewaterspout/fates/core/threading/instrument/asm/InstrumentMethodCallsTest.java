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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Callable;

import com.github.upthewaterspout.fates.core.threading.instrument.agent.FatesMethodEntryExitFilter;
import com.github.upthewaterspout.fates.core.threading.instrument.asm.instrumented.ClassWithMethodCall;
import org.junit.Test;

public class InstrumentMethodCallsTest extends InstrumentationTest {

  @Override
  protected MethodEntryExitFilter getMethodEntryExitFilter() {
    return new FatesMethodEntryExitFilter(ClassWithMethodCall.class.getName());
  }

  @Test
  public void methodCallsAreInstrumented() throws Exception {
    String className = ClassWithMethodCall.class.getCanonicalName();
    Callable object = transformAndCreate(className);
    object.call();
    verify(hook, times(1)).beforeMethod(className, "doSomething");
    verify(hook, times(1)).afterMethod(className, "doSomething");

    verify(hook, times(1)).beforeMethod(className, "<clinit>");
    verify(hook, times(1)).afterMethod(className, "<clinit>");
  }

}