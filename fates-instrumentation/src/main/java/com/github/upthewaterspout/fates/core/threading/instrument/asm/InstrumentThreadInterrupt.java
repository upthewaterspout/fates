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

import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventSingleton;
import com.github.upthewaterspout.fates.core.threading.instrument.asm.ReplaceMethodCall.MethodCall;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Replaces all calls to {@link Thread#interrupt()} ()} with  calls to {@link ExecutionEventSingleton#replaceInterrupt(Thread)}
 */
public class InstrumentThreadInterrupt extends AbstractClassVisitor {

  public InstrumentThreadInterrupt(ClassVisitor cv) {
    super(build(cv));
  }

  private static ClassVisitor build(ClassVisitor cv) {
    cv = new ReplaceMethodCall(cv,
        new MethodCall(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Thread.class), "interrupt", "()V"),
        new MethodCall(Opcodes.INVOKESTATIC, Type.getInternalName(ExecutionEventSingleton.class), "replaceInterrupt",
            Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Thread.class))));

    cv = new ReplaceMethodCall(cv,
        new MethodCall(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Thread.class), "isInterrupted", Type.getMethodDescriptor(Type.BOOLEAN_TYPE)),
        new MethodCall(Opcodes.INVOKESTATIC, Type.getInternalName(ExecutionEventSingleton.class), "replaceIsInterrupted",
            Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(Thread.class))));

    cv = new ReplaceMethodCall(cv,
        new MethodCall(Opcodes.INVOKESTATIC, Type.getInternalName(Thread.class), "interrupted", Type.getMethodDescriptor(Type.BOOLEAN_TYPE)),
        new MethodCall(Opcodes.INVOKESTATIC, Type.getInternalName(ExecutionEventSingleton.class), "replaceInterrupted",
            Type.getMethodDescriptor(Type.BOOLEAN_TYPE)));

    return cv;
  }
}

