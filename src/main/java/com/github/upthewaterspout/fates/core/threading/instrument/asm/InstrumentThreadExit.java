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
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * Adds a call to {@link ExecutionEventSingleton#beforeThreadExit()} when a thread
 * is exiting.
 */
public class InstrumentThreadExit extends AbstractClassVisitor {

  public InstrumentThreadExit(final ClassVisitor cv) {
    super(cv);
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    if(getClassName().equals("java/lang/Thread") && name.equals("exit")) {
      final MethodVisitor delegate = super.visitMethod(access, name, desc, signature, exceptions);
      return new InstrumentingMethodVisitor(Opcodes.ASM5, delegate, access, name, desc);
    } else {
      return super.visitMethod(access, name, desc, signature, exceptions);
    }
  }

  private class InstrumentingMethodVisitor extends AdviceAdapter {

    /**
     * Creates a new {@link AdviceAdapter}.
     * @param api the ASM API version implemented by this visitor. Must be one
     * of {@link Opcodes#ASM4} or {@link Opcodes#ASM5}.
     * @param mv the method visitor to which this adapter delegates calls.
     * @param access the method's access flags (see {@link Opcodes}).
     * @param name the method's name.
     * @param desc the method's descriptor (see {@link Type Type}).
     */
    protected InstrumentingMethodVisitor(final int api,
                                         final MethodVisitor mv,
                                         final int access,
                                         final String name,
                                         final String desc)
    {
      super(api, mv, access, name, desc);
    }

    @Override
    protected void onMethodEnter() {
      visitMethodInsn(INVOKESTATIC,
          "com/github/upthewaterspout/fates/core/threading/instrument/ExecutionEventSingleton", "beforeThreadExit", "()V", false);
      visitLabel(new Label());
      super.onMethodEnter();
    }
  }

}
