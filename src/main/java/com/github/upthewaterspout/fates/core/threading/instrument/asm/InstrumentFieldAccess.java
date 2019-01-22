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
import static org.objectweb.asm.Opcodes.DUP2;
import static org.objectweb.asm.Opcodes.DUP2_X1;
import static org.objectweb.asm.Opcodes.DUP_X2;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.POP2;
import static org.objectweb.asm.Opcodes.PUTSTATIC;

import java.util.HashSet;
import java.util.Set;

import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventSingleton;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
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
          callBeforeGetField(opcode, owner, getClassName(), getMethodName(), getLastLineNumber());
        } else if(isFieldUpdate(opcode)) {
          callBeforeSetField(opcode, owner, Type.getType(desc), getClassName(), getMethodName(), getLastLineNumber());
        }
      }

      super.visitFieldInsn(opcode, owner, name, desc);
    }

    protected void callBeforeGetField(int opcode, String owner, String className, String methodName, int lineNumber) {
      if(opcode == GETSTATIC) {
        visitLdcInsn(Type.getObjectType(owner));
      } else {
        visitInsn(DUP);
      }
      putClassMethodAndLine(className, methodName, lineNumber);
      visitMethodInsn(Opcodes.INVOKESTATIC,
          "com/github/upthewaterspout/fates/core/threading/instrument/ExecutionEventSingleton", "beforeGetField", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;I)V", false);
    }

    protected void callBeforeSetField(int opcode, String owner, Type fieldType, String className,
                                      String methodName, int lineNumber) {

      if(opcode == PUTSTATIC) {
        callBeforeStaticSetField(owner, fieldType, className, methodName, lineNumber);
      } else {
        callBeforeSetInstanceField(fieldType, className, methodName, lineNumber);
      }
    }

    private void callBeforeSetInstanceField(Type fieldType, String className, String methodName,
                                            int lineNumber) {
      if (isDoubleOrlong(fieldType)) {
        //Push the owner and null onto the stack
        //we don't need the value of the field
        putOwnerAndNull();

      } else if( isPrimitive(fieldType)) {
        //Stack = owner, value

        visitInsn(DUP2);
        visitInsn(POP);
        visitInsn(Opcodes.ACONST_NULL);

        //Stack = owner, value, owner, null
      } else {
        //Stack = owner, value


        visitInsn(DUP2);

        //Stack = owner, value, owner, value
      }

      putClassMethodAndLine(className, methodName, lineNumber);
      invokeSetFieldHook();
    }

    private void putOwnerAndNull() {
      //Stack  = owner, valueHigh, valueLow

      visitInsn(DUP2_X1);

      //Stack  = valueHigh, valueLow, owner, valueHigh, valueLow

      visitInsn(POP2);

      //Stack  = valueHigh, valueLow, owner

      visitInsn(DUP_X2);

      //Stack = owner, valueHigh, valueLow, owner

      visitInsn(Opcodes.ACONST_NULL);
    }

    private void invokeSetFieldHook() {
      visitMethodInsn(Opcodes.INVOKESTATIC,
          "com/github/upthewaterspout/fates/core/threading/instrument/ExecutionEventSingleton", "beforeSetField", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;I)V", false);
    }

    private void putClassMethodAndLine(String className, String methodName, int lineNumber) {
      visitLdcInsn(className.replace('/', '.'));
      visitLdcInsn(methodName);
      visitIntInsn(Opcodes.SIPUSH, lineNumber);
    }

    private void callBeforeStaticSetField(String owner, Type fieldType, String className,
                                          String methodName, int lineNumber) {
      if(isPrimitive(fieldType)) {
        visitLdcInsn(Type.getObjectType(owner));
        visitInsn(Opcodes.ACONST_NULL);
      } else {

        //Stack  = value
        visitInsn(DUP);

        //Stack  = value, value

        visitLdcInsn(Type.getObjectType(owner));

        //Stack  = value, value, owner_class

        visitInsn(Opcodes.SWAP);

        //Stack  = value, owner_class, value
      }

      putClassMethodAndLine(className, methodName, lineNumber);
      invokeSetFieldHook();
    }


    private boolean isFieldRead(int opcode) {
      switch (opcode) {
        case Opcodes.GETFIELD:
        case GETSTATIC:
          return true;
        default:
          return false;
      }
    }

    private boolean isFieldUpdate(int opcode) {
      switch (opcode) {
        case Opcodes.PUTFIELD:
        case PUTSTATIC:
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

  private boolean isDoubleOrlong(Type fieldType) {

    switch(fieldType.getSort()) {
      case Type.DOUBLE:
      case Type.LONG:
        return true;
      default:
        return false;
    }
  }

}
