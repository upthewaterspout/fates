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

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * Instrument calls to create an object. This class adds a call to afterNew immediately
 * after the constructor for java.lang.object is called. We instrument this point rather than
 * after the NEW opcode because because we can't actually pass the object to the hook until this
 * constructor is called. This should still happen
 */
public class InstrumentNewObject  extends AbstractClassVisitor  {
  public InstrumentNewObject(ClassVisitor cv) {
    super(cv);
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature,
                                   String[] exceptions) {
    final MethodVisitor delegate = super.visitMethod(access, name, desc, signature, exceptions);
    return new InstrumentNew(Opcodes.ASM7, delegate, access, name, desc);
  }

  public static class InstrumentNew extends AdviceAdapter {

    protected InstrumentNew(int api, MethodVisitor methodVisitor, int access, String name,
                            String descriptor) {
      super(api, methodVisitor, access, name, descriptor);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor,
                                boolean isInterface) {
      if(isObjectConstructor(opcode, owner, name, descriptor, isInterface)) {
        addAfterNewCall(opcode, owner, name, descriptor, isInterface);
      } else {
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
      }
    }

    private void addAfterNewCall(int opcode, String owner, String name, String descriptor,
                                 boolean isInterface) {
      //Make a copy of the object on the stack
      visitInsn(DUP);

      super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);

      //Invoke the hook
      visitMethodInsn(INVOKESTATIC,
          "com/github/upthewaterspout/fates/core/threading/instrument/ExecutionEventSingleton", "afterNew",
          "(Ljava/lang/Object;)V", false);
    }
  }

  private static boolean isObjectConstructor(int opcode, String owner, String name, String descriptor,
                                      boolean isInterface) {
    return opcode == Opcodes.INVOKESPECIAL && owner.equals("java/lang/Object") && name.equals("<init>");
  }
}
