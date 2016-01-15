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


/**
 * Adds calls to {@link ExecutionEventSingleton#beforeSetField(String, String, int)} and
 * {@link ExecutionEventSingleton#beforeSetField(String, String, int)} before all
 * field access.
 */
public class InstrumentFieldAccess extends AbstractClassVisitor {

  public InstrumentFieldAccess(ClassVisitor cv) {
    super(cv);
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    return new FieldAccessHookMethodVisitor(name, super.visitMethod(access, name, desc, signature, exceptions));
  }

  private class FieldAccessHookMethodVisitor extends HookMethodVisitor {

    public FieldAccessHookMethodVisitor(String methodName, MethodVisitor mv) {
      super(methodName, mv);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
      if (isFieldRead(opcode)) {
        callBeforeGetField(this, getClassName(), getMethodName(), getLastLineNumber());
      } else if(isFieldUpdate(opcode)) {
        callBeforeSetField(this, getClassName(), getMethodName(), getLastLineNumber());
      }
      super.visitFieldInsn(opcode, owner, name, desc);
    }

    protected void callBeforeGetField(MethodVisitor mv, String className, String methodName, int lineNumber) {
      mv.visitLdcInsn(className.replace('/', '.'));
      mv.visitLdcInsn(methodName);
      mv.visitIntInsn(Opcodes.SIPUSH, lineNumber);
      mv.visitMethodInsn(Opcodes.INVOKESTATIC,
          "com/github/upthewaterspout/fates/core/threading/instrument/ExecutionEventSingleton", "beforeGetField", "(Ljava/lang/String;Ljava/lang/String;I)V", false);
      mv.visitLabel(new Label());
    }

    protected void callBeforeSetField(MethodVisitor mv, String className, String methodName, int lineNumber) {
      mv.visitLdcInsn(className.replace('/', '.'));
      mv.visitLdcInsn(methodName);
      mv.visitIntInsn(Opcodes.SIPUSH, lineNumber);
      mv.visitMethodInsn(Opcodes.INVOKESTATIC,
          "com/github/upthewaterspout/fates/core/threading/instrument/ExecutionEventSingleton", "beforeSetField", "(Ljava/lang/String;Ljava/lang/String;I)V", false);
      mv.visitLabel(new Label());
    }

    private boolean isFieldRead(int opcode) {
      switch (opcode) {
        case Opcodes.GETFIELD:
        case Opcodes.GETSTATIC:
          return true;
        default:
          return false;
      }
    }

    private boolean isFieldUpdate(int opcode) {
      switch (opcode) {
        case Opcodes.PUTFIELD:
        case Opcodes.PUTSTATIC:
          return true;
        default:
          return false;
      }
    }
  }

}
