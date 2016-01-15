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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventSingleton;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Thread has some synchronized methods that must include before/after synchronization calls.
 *
 * However, because the JVM has already defined Thread, we can't use the approach in {@link InstrumentSynchronizedMethod}
 * to remove the synchronization.
 *
 * Therefore, this class replaces all <i> calls </i> to these methods with its own methods
 * that do the necessary before/after sync calls.
 */
public class InstrumentThreadSynchronizedMethods extends AbstractClassVisitor {

  public static final String THREAD = "java/lang/Thread";
  public static final String INTERNAL_NAME = Type.getInternalName(InstrumentThreadSynchronizedMethods.class);
  public static final String NEXT_THREAD_ID = "nextThreadID";

  public InstrumentThreadSynchronizedMethods(final ClassVisitor cv) {
    super(build(cv));
  }

  private static ClassVisitor build(ClassVisitor cv) {
    cv = new ReplaceMethodCall(cv,
        new ReplaceMethodCall.MethodCall(Opcodes.INVOKEVIRTUAL, THREAD, "start", "()V"),
        new ReplaceMethodCall.MethodCall(Opcodes.INVOKESTATIC,
            Type.getInternalName(InstrumentThreadSynchronizedMethods.class),
            "replaceStart", "(Ljava/lang/Thread;)V"));

    cv = new ReplaceMethodCall(cv,
        new ReplaceMethodCall.MethodCall(Opcodes.INVOKEVIRTUAL, THREAD, "setName", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class))),
        new ReplaceMethodCall.MethodCall(Opcodes.INVOKESTATIC, INTERNAL_NAME, "replaceSetName", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Thread.class), Type.getType(String.class))));

    cv = new ReplaceMethodCall(cv,
        new ReplaceMethodCall.MethodCall(Opcodes.INVOKESTATIC, THREAD, NEXT_THREAD_ID, Type.getMethodDescriptor(Type.VOID_TYPE)),
        new ReplaceMethodCall.MethodCall(Opcodes.INVOKESTATIC, INTERNAL_NAME, "replaceNextThreadID", Type.getMethodDescriptor(Type.VOID_TYPE)));

    return cv;
  }

  public static void replaceStart(Thread thread) {
    ExecutionEventSingleton.beforeThreadStart(thread);
    ExecutionEventSingleton.beforeSynchronization(thread);
    thread.start();
    ExecutionEventSingleton.afterSynchronization(thread);
    ExecutionEventSingleton.afterThreadStart(thread);
  }

  public static void replaceSetName(Thread thread, String name) {
    ExecutionEventSingleton.beforeSynchronization(thread);
    thread.setName(name);
    ExecutionEventSingleton.afterSynchronization(thread);
  }

  public static void replaceNextThreadID() {
    ExecutionEventSingleton.beforeSynchronization(Thread.class);
    try {
      Method method = Thread.class.getDeclaredMethod(NEXT_THREAD_ID);
      method.setAccessible(true);
      method.invoke(null);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException("Unable to invoke private method on thread", e);
    } finally {
      ExecutionEventSingleton.afterSynchronization(Thread.class);
    }

  }
}
