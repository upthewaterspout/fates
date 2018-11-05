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

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Callable;

import com.github.upthewaterspout.fates.core.threading.instrument.asm.instrumented.ClassWithAnonymousInnerClass;
import com.github.upthewaterspout.fates.core.threading.instrument.asm.instrumented.ClassWithFieldAccess;
import com.github.upthewaterspout.fates.core.threading.instrument.asm.instrumented.ClassWithObjectRefences;
import org.junit.Test;

public class InstrumentFieldAccessTest extends InstrumentationTest {

  @Test
  public void fieldAccessAddsHook() throws Exception {
    String className = ClassWithFieldAccess.class.getCanonicalName();
    Callable object = transformAndCreate(className);
    object.call();
    verify(hook, times(1)).beforeGetField(eq(className), eq("call"), eq(26));
    verify(hook, times(1)).beforeSetField(eq(object), eq(null), eq(className), eq("call"), eq(26));
    verify(hook, times(1)).beforeGetField(eq(className), eq("call"), eq(27));
  }

  @Test
  public void fieldAccessTracksOwnerAndFieldValue() throws Exception {
    String className = ClassWithObjectRefences.class.getCanonicalName();
    Callable object = transformAndCreate(className);
    Object fieldValue = object.call();
    verify(hook, times(1)).beforeSetField(eq(object), eq(fieldValue), eq(className), eq("call"), eq(30));
  }

  @Test
  public void anonymousClassTracksEnclosingClass() throws Exception {
    String className = ClassWithAnonymousInnerClass.class.getCanonicalName();
    Callable object = transformAndCreate(className);
    Object innerClass = object.call();
    verify(hook, times(1)).beforeSetField(eq(innerClass), eq(object), eq(className), eq("call"), eq(30));
  }

}