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

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.util.concurrent.locks.LockSupport;

import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventSingleton;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Replace calls to {@link LockSupport} with calls to {@link ExecutionEventSingleton}
 *
 * TODO - implement this using {@link ReplaceMethodCall}
 */
public class InstrumentLockSupport extends AbstractClassVisitor {

  public InstrumentLockSupport(final ClassVisitor cv) {
    super(cv);
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    final MethodVisitor delegate = super.visitMethod(access, name, desc, signature, exceptions);
    return new ReplaceParkAndUnpark(Opcodes.ASM7, delegate, access, name, desc);
  }

  private class ReplaceParkAndUnpark extends MethodVisitor {

    protected ReplaceParkAndUnpark(final int api,
                                   final MethodVisitor mv,
                                   final int access,
                                   final String name,
                                   final String desc)
    {
      super(api, mv);
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name,
                                final String desc, final boolean itf) {
      if(opcode == INVOKESTATIC && owner.equals("java/util/concurrent/locks/LockSupport") && name.equals("park")) {
        visitMethodInsn(INVOKESTATIC,
            "com/github/upthewaterspout/fates/core/threading/instrument/ExecutionEventSingleton", "replacePark", desc, false);
      } else if(opcode == INVOKESTATIC && owner.equals("java/util/concurrent/locks/LockSupport") && name.equals("parkNanos")) {
        visitMethodInsn(INVOKESTATIC,
            "com/github/upthewaterspout/fates/core/threading/instrument/ExecutionEventSingleton", "replaceParkNanos", desc, false);
      } else if(opcode == INVOKESTATIC && owner.equals("java/util/concurrent/locks/LockSupport") && name.equals("parkUntil")) {
        visitMethodInsn(INVOKESTATIC,
            "com/github/upthewaterspout/fates/core/threading/instrument/ExecutionEventSingleton", "replaceParkUntil", desc, false);
      } else if(opcode == INVOKESTATIC && owner.equals("java/util/concurrent/locks/LockSupport") && name.equals("unpark")) {
        visitMethodInsn(INVOKESTATIC,
            "com/github/upthewaterspout/fates/core/threading/instrument/ExecutionEventSingleton",
            "replaceUnpark", desc, false);
      } else {
        super.visitMethodInsn(opcode, owner, name, desc, itf);
      }
    }
  }
}
