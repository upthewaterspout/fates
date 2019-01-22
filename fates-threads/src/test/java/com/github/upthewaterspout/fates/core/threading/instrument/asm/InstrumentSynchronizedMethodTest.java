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

import static java.lang.Thread.holdsLock;
import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import com.github.upthewaterspout.fates.core.threading.instrument.asm.instrumented.ClassWithSynchronizedMethod;
import com.github.upthewaterspout.fates.core.threading.instrument.asm.instrumented.ClassWithSynchronizedMethodCallsInBranch;
import com.github.upthewaterspout.fates.core.threading.instrument.classloader.TransformingClassLoader;
import org.junit.Test;
import org.mockito.stubbing.Answer;

public class InstrumentSynchronizedMethodTest extends InstrumentationTest {
  @Test
  public void synchronizedMethodAddsHook() throws Exception {
    String className = ClassWithSynchronizedMethod.class.getCanonicalName();
    Callable object = transformAndCreate(className);
    final Answer verifyNotSynchronized = invocation -> {
      assertFalse(holdsLock(object));
      return null;
    };
    doAnswer(verifyNotSynchronized).when(hook).beforeSynchronization(eq(object));
    doAnswer(verifyNotSynchronized).when(hook).afterSynchronization(eq(object));
    object.call();
    verify(hook, times(1)).beforeSynchronization(eq(object));
    verify(hook, times(1)).afterSynchronization(eq(object));
  }

  @Test
  public void staticSynchronizedMethodAddsHook() throws Exception {
    String className = ClassWithSynchronizedMethod.class.getCanonicalName();
    TransformingClassLoader loader = new TransformingClassLoader(transformer, className);
    Class<?> clazz = loader.loadClass(className);
    Method method = clazz.getMethod("staticCall");
    final Answer verifyNotSynchronized = invocation -> {
      assertFalse(Thread.currentThread().holdsLock(clazz));
      return null;
    };
    doAnswer(verifyNotSynchronized).when(hook).beforeSynchronization(eq(clazz));
    doAnswer(verifyNotSynchronized).when(hook).afterSynchronization(eq(clazz));
    method.invoke(null);
    verify(hook, times(1)).beforeSynchronization(eq(clazz));
    verify(hook, times(1)).afterSynchronization(eq(clazz));
  }

  @Test
  public void callingSynchronizedMethodsWithBranchesAddsHook() throws Exception {
    String className = ClassWithSynchronizedMethodCallsInBranch.class.getCanonicalName();
    TransformingClassLoader loader = new TransformingClassLoader(transformer, className);
    Class<?> clazz = loader.loadClass(className);
    Callable object = (Callable) clazz.newInstance();
    final Answer verifyNotSynchronized = invocation -> {
      assertFalse(holdsLock(clazz));
      assertFalse(holdsLock(object));
      return null;
    };
    doAnswer(verifyNotSynchronized).when(hook).beforeSynchronization(eq(clazz));
    doAnswer(verifyNotSynchronized).when(hook).afterSynchronization(eq(clazz));
    Object sync = object.call();
    verify(hook, times(2)).beforeSynchronization(eq(object));
    verify(hook, times(2)).afterSynchronization(eq(object));
  }

}