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

import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.util.Objects;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * {@link ClassVisitor} that will replace calls from one method to calls to another
 * method. *This does not modify the stack!* The new method should consume the same arguments
 * that the old method did.
 */
public class ReplaceMethodCall extends AbstractClassVisitor {

  private final MethodCall oldMethod;
  private final MethodCall newMethod;

  public ReplaceMethodCall(final ClassVisitor cv, MethodCall oldMethod, MethodCall newMethod) {
    super(cv);
    this.oldMethod = oldMethod;
    this.newMethod = newMethod;
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
      final MethodVisitor delegate = super.visitMethod(access, name, desc, signature, exceptions);
      return new ReplacingMethodVisitor(Opcodes.ASM7, delegate, access, name, desc);
  }

  private class ReplacingMethodVisitor extends MethodVisitor {

    protected ReplacingMethodVisitor(final int api,
                                     final MethodVisitor mv,
                                     final int access,
                                     final String name,
                                     final String desc)
    {
      super(api, mv);
    }

    @Override
    public void visitMethodInsn(final int opcode,
                                final String owner,
                                final String name,
                                final String desc,
                                final boolean itf)
    {
      if(!oldMethod.matches(opcode, owner, name, desc)) {
        super.visitMethodInsn(opcode, owner, name, desc, itf);
        return;
      }

      visitMethodInsn(INVOKESTATIC,
          newMethod.owner,
          newMethod.name,
          newMethod.desc,
      false);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
      for(int i = 0; i < bsmArgs.length; i++) {
        Object arg = bsmArgs[i];
        if((arg instanceof Handle) && oldMethod.matches((Handle) arg)) {
          bsmArgs[i] = new Handle(H_INVOKESTATIC, newMethod.owner, newMethod.name, newMethod.desc, false);
          break;
        }
      }
      super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
    }
  }

  public static class MethodCall {
    final int opcode;
    final String owner;
    final String name;
    final String desc;

    public MethodCall(int opcode, String owner, String name, String desc) {
      this.opcode = opcode;
      this.owner = owner;
      this.name = name;
      this.desc = desc;
    }

    public boolean matches(int opcode, String owner, String name, String desc) {
      return new MethodCall(opcode, owner,name, desc).equals(this);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      MethodCall that = (MethodCall) o;
      return opcode == that.opcode &&
          Objects.equals(owner, that.owner) &&
          Objects.equals(name, that.name) &&
          Objects.equals(desc, that.desc);
    }

    @Override
    public int hashCode() {
      return Objects.hash(opcode, owner, name, desc);
    }

    public boolean matches(Handle arg) {
      return arg.getOwner().equals(owner) && arg.getName().equals(name) && arg.getDesc().equals(desc);
    }
  }
}
