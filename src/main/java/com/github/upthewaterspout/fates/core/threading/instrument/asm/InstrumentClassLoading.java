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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * Replaces all calls to {@link Thread#join()} with  calls to {@link ExecutionEventSingleton#replaceJoin(Thread)}
 */
public class InstrumentClassLoading extends AbstractClassVisitor {

  public InstrumentClassLoading(ClassVisitor cv) {
    super(cv);
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    final MethodVisitor delegate = super.visitMethod(access, name, desc, signature, exceptions);
    if(name.equals("loadClass")) {
      return new InstrumentMethod(Opcodes.ASM5, delegate, access, name, desc);
    } else {
      return delegate;
    }
  }

  private class InstrumentMethod extends AdviceAdapter {
    protected InstrumentMethod(int api, MethodVisitor mv, int access, String name, String desc) {
      super(api, mv, access, name, desc);
    }

    @Override
    protected void onMethodEnter() {
      mv.visitMethodInsn(Opcodes.INVOKESTATIC,
          Type.getInternalName(ExecutionEventSingleton.class), "beforeLoadClass", "()V", false);

      super.onMethodEnter();
    }

    @Override
    protected void onMethodExit(int opcode) {
      mv.visitMethodInsn(Opcodes.INVOKESTATIC,
          Type.getInternalName(ExecutionEventSingleton.class), "afterLoadClass", "()V", false);

      super.onMethodExit(opcode);
    }
  }
}

