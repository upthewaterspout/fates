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

package com.github.upthewaterspout.fates.core.threading.instrument.agent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;

import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventSingleton;
import com.github.upthewaterspout.fates.core.threading.instrument.asm.AsmTransformer;
import com.github.upthewaterspout.fates.core.threading.instrument.asm.MethodEntryExitFilter;

/**
 * Java agent class to initialize our bytecode manipulating agent.
 */
public class FatesAgent {
  public static void premain(String agentArgs, Instrumentation inst) {

    String[] methodEntryExitEnabledClasses = agentArgs.split(",");

    MethodEntryExitFilter filter = new FatesMethodEntryExitFilter(methodEntryExitEnabledClasses);
    FilterTransformer transformer = new FilterTransformer(
        new AsmTransformer(filter), "com/github/upthewaterspout/fates/core", "com/intellij", "java/lang/ThreadLocal", "java/lang/ref/WeakReference", "java/lang/ref/Reference", "java/lang/VerifyError", "java/lang/LinkageError");
    inst.addTransformer(transformer, true);

    ArrayList<Class<?>> toTransform = new ArrayList<>();
    for(Class<?> clazz : inst.getAllLoadedClasses()) {
      if(!clazz.isPrimitive() && !clazz.isArray() && !clazz.equals(Object.class)) {
        toTransform.add(clazz);
      }
    }

    try {
      inst.retransformClasses(toTransform.toArray(new Class[0]));
    } catch (UnmodifiableClassException e) {
      throw new RuntimeException("Could not transform previously defined classes: " + e);
    }

    ExecutionEventSingleton.setAvailable();
  }

  public static void agentmain(String agentArgs, Instrumentation inst) throws IOException {
    premain(agentArgs, inst);
  }

}
