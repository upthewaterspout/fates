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
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * Replaces all calls to {@link Thread#join()} with  calls to {@link ExecutionEventSingleton#replaceJoin(Thread)}
 */
public class InstrumentMethodCalls extends AbstractClassVisitor {

  private final MethodEntryExitFilter filter;

  public InstrumentMethodCalls(ClassVisitor cv, MethodEntryExitFilter filter) {
    super(cv);
    this.filter = filter;
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    final MethodVisitor delegate = super.visitMethod(access, name, desc, signature, exceptions);
    return new InstrumentMethod(Opcodes.ASM7, delegate, access, name, desc);
  }

  private class InstrumentMethod extends AdviceAdapter {
    protected InstrumentMethod(int api, MethodVisitor mv, int access, String name, String desc) {
      super(api, mv, access, name, desc);
    }

    @Override
    protected void onMethodEnter() {
      if(filter.test(getBinaryClassName(), getMethodName())) {
        pushClassAndMethod();
        SingletonCall
            .add(this, "beforeMethod", Type.VOID_TYPE, SingletonCall.STRING, SingletonCall.STRING);
      }
      super.onMethodEnter();
    }

    private void pushClassAndMethod() {
      String className = getClassName();
      String methodName = getMethodName();

      visitLdcInsn(className.replace('/', '.'));
      visitLdcInsn(methodName);
    }

    @Override
    protected void onMethodExit(int opcode) {
      if(filter.test(getBinaryClassName(), getMethodName())) {
        pushClassAndMethod();
        SingletonCall
            .add(this, "afterMethod", Type.VOID_TYPE, SingletonCall.STRING, SingletonCall.STRING);
      }

      super.onMethodExit(opcode);
    }
  }

  private String getBinaryClassName() {
    return Type.getObjectType(getClassName()).getClassName();
  }

}

