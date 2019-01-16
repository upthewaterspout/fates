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
import org.objectweb.asm.Opcodes;

/**
 * A visitor that sets the minimum java version of the generated class file
 * to V1_5.
 *
 * This visitor is needed because some of the bytecode we generate requires a version greater
 * than or equal to V1_5, specifically LDC of a class constant in {@link InstrumentFieldAccess}
 */
public class MinimumVersionVisitor extends ClassVisitor {
  public MinimumVersionVisitor(ClassVisitor transformingVisitor) {
    super(Opcodes.ASM7, transformingVisitor);
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName,
                    String[] interfaces) {
    if(version < Opcodes.V1_5) {
      version = Opcodes.V1_5;
    }

    super.visit(version, access, name, signature, superName, interfaces);
  }
}
