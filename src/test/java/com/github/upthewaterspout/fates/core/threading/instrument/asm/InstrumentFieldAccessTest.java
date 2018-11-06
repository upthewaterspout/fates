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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.concurrent.Callable;

import com.github.upthewaterspout.fates.core.threading.instrument.asm.instrumented.ClassWithAnonymousInnerClass;
import com.github.upthewaterspout.fates.core.threading.instrument.asm.instrumented.ClassWithFieldAccess;
import com.github.upthewaterspout.fates.core.threading.instrument.asm.instrumented.ClassWithObjectRefences;
import com.github.upthewaterspout.fates.core.threading.instrument.asm.instrumented.ClassWithStaticFieldAccess;
import org.junit.Test;

public class InstrumentFieldAccessTest extends InstrumentationTest {

  @Test
  public void fieldAccessAddsHook() throws Exception {
    String className = ClassWithFieldAccess.class.getCanonicalName();
    Callable<Integer> object = transformAndCreate(className);
    int value = object.call();
    assertEquals(6, value);
    verify(hook, times(1)).beforeGetField(eq(className), eq("call"), eq(26));
    verify(hook, times(1)).beforeSetField(eq(object), eq(null), eq(className), eq("call"), eq(26));
    verify(hook, times(1)).beforeGetField(eq(className), eq("call"), eq(27));
  }

  @Test
  public void staticFieldAccessAddsHook() throws Exception {
    String className = ClassWithStaticFieldAccess.class.getCanonicalName();
    Callable<Integer> object = transformAndCreate(className);
    int value = object.call();
    assertEquals(6, value);
    verify(hook, times(1)).beforeGetField(eq(className), eq("call"), eq(30));
    verify(hook, times(1)).beforeSetField(eq(object.getClass()), eq(null), eq(className), eq("call"), eq(30));
    verify(hook, times(1)).beforeGetField(eq(className), eq("call"), eq(31));
  }

  @Test
  public void fieldAccessTracksOwnerAndFieldValue() throws Exception {
    String className = ClassWithObjectRefences.class.getCanonicalName();
    Callable object = transformAndCreate(className);
    Object fieldValue = object.call();
    verify(hook, times(1)).beforeSetField(eq(object), eq(fieldValue), eq(className), eq("call"), eq(30));
  }

  /**
   * Verify that synthetic fields are not instrumented
   * We would like to instrument these, but the JVM will not
   * allow us to the access *this* in the constructor of the
   * inner class before the super constructor is called, and these
   * fields are initialized before the super constructor call
   *
   * see {@link InstrumentFieldAccess#isFinal(String, String)}
   * @throws Exception
   */
  @Test
  public void syntheticFieldsAreNotInstrumented() throws Exception {
    String className = ClassWithAnonymousInnerClass.class.getCanonicalName();
    Callable object = transformAndCreate(className);
    Object innerClass = object.call();
    verifyNoMoreInteractions(hook);
  }

}