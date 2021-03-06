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

import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventSingleton;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;


/**
 * Adds calls to {@link ExecutionEventSingleton#beforeSynchronization(Object)} and
 * {@link ExecutionEventSingleton#afterSynchronization(Object)} for synchronized
 * blocks.
 */
public class InstrumentSynchronizedBlock extends AbstractClassVisitor {
  public InstrumentSynchronizedBlock(ClassVisitor cv) {
    super(cv);
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    final MethodVisitor delegate = super.visitMethod(access, name, desc, signature, exceptions);
    return new InstrumentSynchronization(Opcodes.ASM7, delegate, access, name, desc);
  }

  private class InstrumentSynchronization extends MethodVisitor {

    protected InstrumentSynchronization(final int api,
                                        final MethodVisitor mv,
                                        final int access,
                                        final String name,
                                        final String desc)
    {
      super(api, mv);
    }


    @Override
    public void visitInsn(int opcode) {
      if (Opcodes.MONITORENTER == opcode) {
        visitInsn(DUP);
        SingletonCall.add(this, "beforeSynchronization", Type.VOID_TYPE, SingletonCall.OBJECT);
        super.visitInsn(opcode);
      } else if (Opcodes.MONITOREXIT == opcode) {
        visitInsn(DUP);
        super.visitInsn(opcode);
        SingletonCall.add(this, "afterSynchronization", Type.VOID_TYPE, SingletonCall.OBJECT);

      } else {
        super.visitInsn(opcode);
      }
    }
  }

}
