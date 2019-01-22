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

import java.lang.reflect.Method;

import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventSingleton;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Utility for adding calls to {@link ExecutionEventSingleton}
 */
public class SingletonCall {

  public static final Type OBJECT = Type.getType(Object.class);
  public static final Type STRING = Type.getType(String.class);

  /**
   * Add a call to {@link ExecutionEventSingleton in this method}. It is the responsibility
   * of the caller of this method to have already prepared the stack correctly!
   * @param methodName The name of the method
   * @param returnType The return type of the method
   * @param parameterTypes The parameter types of the method
   */
  public static void add(MethodVisitor methodVisitor, String methodName, Type returnType, Type ... parameterTypes) {

    methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC,
        Type.getInternalName(ExecutionEventSingleton.class), methodName, Type.getMethodDescriptor(returnType, parameterTypes), false);

  }
}
