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
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * Replaces {@link Object#wait()} and {@link Object#notify()} with calls to
 * {@link ExecutionEventSingleton#wait()} and {@link ExecutionEventSingleton#notify()}
 */
public class InstrumentWaitNotify extends AbstractClassVisitor {

  public InstrumentWaitNotify(final ClassVisitor cv) {
    super(cv);
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
      final MethodVisitor delegate = super.visitMethod(access, name, desc, signature, exceptions);
      return new InstrumentWaitNotifyCalls(Opcodes.ASM5, delegate, access, name, desc);
  }

  private class InstrumentWaitNotifyCalls extends AdviceAdapter {

    protected InstrumentWaitNotifyCalls(final int api,
                                        final MethodVisitor mv,
                                        final int access,
                                        final String name,
                                        final String desc)
    {
      super(api, mv, access, name, desc);
    }

    @Override
    public void visitMethodInsn(final int opcode,
                                final String owner,
                                final String name,
                                final String desc,
                                final boolean itf)
    {
      if(opcode != Opcodes.INVOKEVIRTUAL || !owner.equals("java/lang/Object")) {
        super.visitMethodInsn(opcode, owner, name, desc, itf);
        return;
      }

      if(name.equals("wait") && desc.equals("()V")) {
        mv.visitMethodInsn(INVOKESTATIC,
            "com/github/upthewaterspout/fates/core/threading/instrument/ExecutionEventSingleton",
            "replaceWait",
            "(Ljava/lang/Object;)V", false);
      } else if(name.equals("wait") && desc.equals("(J)V")) {
        mv.visitMethodInsn(INVOKESTATIC,
            "com/github/upthewaterspout/fates/core/threading/instrument/ExecutionEventSingleton", "replaceWait",
            "(Ljava/lang/Object;J)V", false);
      } else if(name.equals("wait") && desc.equals("(JI)V")) {
        mv.visitMethodInsn(INVOKESTATIC,
            "com/github/upthewaterspout/fates/core/threading/instrument/ExecutionEventSingleton", "replaceWait",
            "(Ljava/lang/Object;JI)V", false);
      } else if(name.equals("notify")) {
        mv.visitMethodInsn(INVOKESTATIC,
            "com/github/upthewaterspout/fates/core/threading/instrument/ExecutionEventSingleton", "replaceNotify",
          "(Ljava/lang/Object;)V", false);
      } else if (name.equals("notifyAll")) {
        mv.visitMethodInsn(INVOKESTATIC,
            "com/github/upthewaterspout/fates/core/threading/instrument/ExecutionEventSingleton", "replaceNotifyAll",
          "(Ljava/lang/Object;)V", false);
      } else {
        super.visitMethodInsn(opcode, owner, name, desc, itf);
      }
    }
  }
}
