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

import java.util.HashSet;
import java.util.Set;

import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventSingleton;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
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

  public Set<String> finalFields = new HashSet<String>();

  public InstrumentFieldAccess(ClassVisitor cv) {
    super(cv);
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    return new FieldAccessHookMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
  }

  @Override
  public FieldVisitor visitField(int access, String name, String desc, String signature,
                                 Object value) {
    if((access & Opcodes.ACC_FINAL) != 0) {
      finalFields.add(name);
    }

    return super.visitField(access, name, desc, signature, value);
  }

  private class FieldAccessHookMethodVisitor extends HookMethodVisitor {

    protected FieldAccessHookMethodVisitor(MethodVisitor mv, int access, String name,
                                           String desc) {
      super(mv, access, name, desc);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
      //Don't instrument final fields.
      if(!isFinal(owner, name)) {
        if (isFieldRead(opcode)) {
          callBeforeGetField(getClassName(), getMethodName(), getLastLineNumber());
        } else if(isFieldUpdate(opcode)) {
          System.err.println("Instrumenting " + owner + "." + name + " at " + getClassName() + ":" + getLastLineNumber());
          callBeforeSetField(opcode, owner, Type.getType(desc), getClassName(), getMethodName(), getLastLineNumber());
        }
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

    protected void callBeforeSetField(int opcode, String owner, Type fieldType, String className,
                                      String methodName, int lineNumber) {
      //Store the field value into a separate variable
      int fieldValueVar = newLocal(fieldType);
      super.visitVarInsn(fieldType.getOpcode(ISTORE), fieldValueVar);

      if(opcode == Opcodes.PUTFIELD) {
        //Duplicate the owner onto the stack
        visitInsn(Opcodes.DUP);
      } else {
        visitLdcInsn(Type.getObjectType(owner));
      }

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

  private boolean isFinal(String owner, String name) {
    return this.finalFields.contains(name) && owner.equals(this.getClassName());
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
