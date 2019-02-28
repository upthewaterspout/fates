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


import static org.objectweb.asm.Opcodes.ASM7;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * A {@link ClassVisitor} that adjusts the maximum stack size up by 5 for all methods.
 *
 *
 * Background - ASMs COMPUTE_MAXS flag is not working on some JDK classes. It appears
 * to be adjusting the stack down for some methods.
 *
 * Rather than using COMPUTE_MAXS, just hardcode an additional n stack elements to every
 * visitMaxs. That is the maximum increase in the stack that our adjustments might make.
 *
 * This bloats the stack with unused space, but it allows the classes to be verified correctly.
 *
 * TODO - only adjust methods that we touched, by the amount we touched them.
 *
 */


public class IncreaseMaxStack extends ClassVisitor {

  private final int adjustment;

  public IncreaseMaxStack(ClassVisitor classVisitor, int adjustment) {
    super(ASM7, classVisitor);
    this.adjustment = adjustment;
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                   String[] exceptions) {
    MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
    return new MaxStackAdjuster(mv, access, name, descriptor);
  }

  public class MaxStackAdjuster extends MethodVisitor {

    protected MaxStackAdjuster(MethodVisitor methodVisitor, int access,
                               String name, String descriptor) {
      super(ASM7, methodVisitor);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
      super.visitMaxs(maxStack + adjustment, maxLocals);
    }
  }
}
