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

/**
 * Replaces all calls to {@link Thread#join()} with  calls to {@link ExecutionEventSingleton#replaceJoin(Thread)}
 */
public class InstrumentJoin extends AbstractClassVisitor {

  public static final String THREAD = "java/lang/Thread";
  public static final String JOIN = "join";
  public static final String REPLACE_JOIN = "replaceJoin";

  public InstrumentJoin(ClassVisitor cv) {
    super(build(cv));
  }

  private static ClassVisitor build(ClassVisitor cv) {
    cv = new ReplaceMethodCall(cv,
        new MethodCall(Opcodes.INVOKEVIRTUAL, THREAD, JOIN, "()V"),
        new MethodCall(Opcodes.INVOKESTATIC, ExecutionEventSingleton.NAME, REPLACE_JOIN,
            "(Ljava/lang/Thread;)V"));

    cv = new ReplaceMethodCall(cv,
        new MethodCall(Opcodes.INVOKEVIRTUAL, THREAD, JOIN, "(J)V"),
        new MethodCall(Opcodes.INVOKESTATIC, ExecutionEventSingleton.NAME, REPLACE_JOIN,
            "(Ljava/lang/Thread;J)V"));

    cv = new ReplaceMethodCall(cv,
        new MethodCall(Opcodes.INVOKEVIRTUAL, THREAD, JOIN, "(JI)V"),
        new MethodCall(Opcodes.INVOKESTATIC, ExecutionEventSingleton.NAME, REPLACE_JOIN,
            "(Ljava/lang/Thread;JI)V"));
    return cv;
  }
}

