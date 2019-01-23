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

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;

/**
 * Pipeline of ASM ClassVisitors that transforms classes, adding
 * the appropriate hooks to control thread ordering
 *
 */
public class AsmTransformer implements ClassFileTransformer {


  public AsmTransformer() {
  }

  @Override
  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                          ProtectionDomain protectionDomain, byte[] classfileBuffer) {
    try {
      ClassReader reader = new ClassReader(classfileBuffer);
      ClassWriter outputWriter = new ClassWriter(reader, 0);
      ClassVisitor transformingVisitor = outputWriter;
      transformingVisitor = new IncreaseMaxStack(transformingVisitor, 5);
      transformingVisitor = new MinimumVersionVisitor(transformingVisitor); //No stack usage
      transformingVisitor = new InstrumentThreadSynchronizedMethods(transformingVisitor); //No stack usage
      transformingVisitor = new InstrumentSynchronizedBlock(transformingVisitor); //One additional stack element
      if(classBeingRedefined == null) {
        transformingVisitor = new InstrumentSynchronizedMethod(transformingVisitor); //One additional stack element
      }
      transformingVisitor = new InstrumentWaitNotify(transformingVisitor); //No stack usage
      transformingVisitor = new InstrumentLockSupport(transformingVisitor); //No stack usage
      transformingVisitor = new InstrumentThreadExit(transformingVisitor); //No stack usage
      transformingVisitor = new InstrumentJoin(transformingVisitor); //No stack usage
      transformingVisitor = new InstrumentFieldAccess(transformingVisitor); //5 stack elements max
      transformingVisitor = new InstrumentMethodCalls(transformingVisitor); //No stack usage
      transformingVisitor = new InstrumentNewObject(transformingVisitor); //One additional stack element
      reader.accept(transformingVisitor, ClassReader.EXPAND_FRAMES);
      byte[] result =  outputWriter.toByteArray();

      new ClassReader(result).accept(new CheckClassAdapter(new ClassWriter(0)), 0);

      return result;
    } catch(Throwable t) {
      System.err.println("Error transforming " + className);
      t.printStackTrace();
      throw t;
    }
  }

}
