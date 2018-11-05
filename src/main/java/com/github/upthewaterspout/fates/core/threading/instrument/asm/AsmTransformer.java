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
      ClassWriter outputWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);

      ClassVisitor transformingVisitor = new InstrumentThreadSynchronizedMethods(outputWriter);
      transformingVisitor = new InstrumentSynchronizedBlock(transformingVisitor);
      if(classBeingRedefined == null) {
        transformingVisitor = new InstrumentSynchronizedMethod(transformingVisitor);
      }
      transformingVisitor = new InstrumentWaitNotify(transformingVisitor);
      transformingVisitor = new InstrumentLockSupport(transformingVisitor);
      transformingVisitor = new InstrumentThreadExit(transformingVisitor);
      transformingVisitor = new InstrumentJoin(transformingVisitor);
      transformingVisitor = new InstrumentFieldAccess(transformingVisitor);
      transformingVisitor = new InstrumentClassLoading(transformingVisitor);
      ClassReader reader = new ClassReader(classfileBuffer);
      reader.accept(transformingVisitor, ClassReader.EXPAND_FRAMES);
      return outputWriter.toByteArray();
    } catch(Throwable t) {
      System.err.println("Error transforming " + className);
      t.printStackTrace();
      throw t;
    }
  }

}
