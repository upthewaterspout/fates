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
import com.sun.org.apache.bcel.internal.generic.ACONST_NULL;
import com.sun.org.apache.bcel.internal.generic.DUP2;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


/**
 * Adds calls to {@link ExecutionEventSingleton#beforeSetField(Object, Object, String, String, int)} and
 * {@link ExecutionEventSingleton#beforeGetField(String, String, int)} before all
 * field access.
 */
public class InstrumentFieldAccess extends AbstractClassVisitor {

  public InstrumentFieldAccess(ClassVisitor cv) {
    super(cv);
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    return new FieldAccessHookMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
  }

  private class FieldAccessHookMethodVisitor extends HookMethodVisitor {

    protected FieldAccessHookMethodVisitor(MethodVisitor mv, int access, String name,
                                           String desc) {
      super(mv, access, name, desc);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
      if (isFieldRead(opcode)) {
        callBeforeGetField(getClassName(), getMethodName(), getLastLineNumber());
      } else if(isFieldUpdate(opcode)) {
        System.err.println("Instrumenting setField " + owner + "." + name + " at " + getClassName() + ":" + getLastLineNumber());
        callBeforeSetField(Type.getType(desc), getClassName(), getMethodName(), getLastLineNumber());
      }
      super.visitFieldInsn(opcode, owner, name, desc);
    }

    protected void callBeforeGetField(String className, String methodName, int lineNumber) {
      visitLdcInsn(className.replace('/', '.'));
      visitLdcInsn(methodName);
      visitIntInsn(Opcodes.SIPUSH, lineNumber);
      visitMethodInsn(Opcodes.INVOKESTATIC,
          "com/github/upthewaterspout/fates/core/threading/instrument/ExecutionEventSingleton", "beforeGetField", "(Ljava/lang/String;Ljava/lang/String;I)V", false);
      visitLabel(new Label());
    }

    protected void callBeforeSetField(Type fieldType, String className, String methodName, int lineNumber) {
      //Store the field value into a separate variable
      int fieldValueVar = newLocal(fieldType);
      super.visitVarInsn(fieldType.getOpcode(ISTORE), fieldValueVar);

      //Duplicate the owner onto the stack
      visitInsn(Opcodes.DUP);

      if(isPrimitive(fieldType)) {
        //If the field is primitive, push null on the stack
        mv.visitInsn(Opcodes.ACONST_NULL);
      } else {
        //Otherwise, push the field value on the stack
        super.visitVarInsn(fieldType.getOpcode(ILOAD), fieldValueVar);
      }

      visitLdcInsn(className.replace('/', '.'));
      visitLdcInsn(methodName);
      visitIntInsn(Opcodes.SIPUSH, lineNumber);
      visitMethodInsn(Opcodes.INVOKESTATIC,
          "com/github/upthewaterspout/fates/core/threading/instrument/ExecutionEventSingleton", "beforeSetField", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;I)V", false);

      visitLabel(new Label());
      //Restore the field value into the stack
      super.visitVarInsn(fieldType.getOpcode(ILOAD), fieldValueVar);
      visitLabel(new Label());
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

  private boolean isPrimitive(Type fieldType) {
    switch (fieldType.getSort()) {
      case Type.OBJECT:
      case Type.ARRAY:
        return false;
      default:
        return true;

    }
  }

}
