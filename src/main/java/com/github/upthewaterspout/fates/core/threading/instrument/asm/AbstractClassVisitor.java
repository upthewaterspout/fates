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
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * Base class for synchronization hook adding visitors with common functionality.
 *
 */
public abstract class AbstractClassVisitor extends ClassVisitor {
  private String className;
  private String methodName;
  private int lastLineNumber;
  private String sourceFile;

  public AbstractClassVisitor(ClassVisitor cv) {
    super(Opcodes.ASM5, cv);
  }

  public void visit(int version, int access, String name, String signature, String superName,
                    String[] interfaces) {
    super.visit(version, access, name, signature, superName, interfaces);
    className = name;
  }

  @Override
  public void visitSource(String source, String debug) {
    super.visitSource(source, debug);
    this.sourceFile = source;
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature,
                                   String[] exceptions) {
    methodName = name;
    return new HookMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
  }
  

  public String getClassName() {
    return className;
  }

  public String getMethodName() {
    return methodName;
  }

  public String getSourceFile() {
    return this.sourceFile;
  }

  public int getLastLineNumber() {
    return lastLineNumber;
  }

  public class HookMethodVisitor extends AdviceAdapter {

    protected HookMethodVisitor(MethodVisitor mv, int access, String name, String desc) {
      super(ASM5, mv, access, name, desc);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
      super.visitLineNumber(line, start);
      lastLineNumber = line;
    }
  }

}
