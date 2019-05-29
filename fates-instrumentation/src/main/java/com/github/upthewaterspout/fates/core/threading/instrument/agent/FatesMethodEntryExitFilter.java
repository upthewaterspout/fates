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

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.URLClassLoader;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.upthewaterspout.fates.core.threading.instrument.asm.MethodEntryExitFilter;

public class FatesMethodEntryExitFilter implements MethodEntryExitFilter {
  public static final String CLASS_INITIALIZER_METHOD_NAME = "<clinit>";

  private static final List<Class<?>> DEFAULT_PUBLIC_ATOMIC_CLASSES = Arrays.asList(
      SecurityManager.class, System.class, AccessControlContext.class, AccessController.class,
      Class.class, ClassLoader.class, URLClassLoader.class, Field.class,
      Integer.class, Long.class, Character.class, Short.class, Byte.class, Boolean.class, Float.class, Double.class, String.class,
      InetAddress.class, ThreadGroup.class);
  private static final List<String> DEFAULT_INTERNAL_ATOMIC_CLASSES = Arrays.asList("java.lang.invoke.MethodHandleNatives", "sun.instrument.InstrumentationImpl");

  public static final List<String> DEFAULT_ATOMIC_CLASS_NAMES;


  static {
    List<String> classNames = new ArrayList<>();
    classNames.addAll(DEFAULT_INTERNAL_ATOMIC_CLASSES);
    for(Class clazz : DEFAULT_PUBLIC_ATOMIC_CLASSES) {
      classNames.add(clazz.getName());
    }
    DEFAULT_ATOMIC_CLASS_NAMES = Collections.unmodifiableList(classNames);
  }

  private final Set<String> allInstrumentedClasses;

  public FatesMethodEntryExitFilter(String ... instrumentedClasses) {

    allInstrumentedClasses = new HashSet<>();
    allInstrumentedClasses.addAll(Arrays.asList(instrumentedClasses));
    allInstrumentedClasses.addAll(DEFAULT_ATOMIC_CLASS_NAMES);
  }

  @Override
  public boolean test(String className, String methodName) {
    if(methodName.equals(CLASS_INITIALIZER_METHOD_NAME)) {
      return true;
    }
    if (allInstrumentedClasses.contains(className)) {
      return true;
    }
    return false;
  }
}
