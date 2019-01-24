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

package com.github.upthewaterspout.fates.core.threading.event;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class IgnoreFinalFieldsListener extends DelegatingExecutionEventListener {

  /**
   * Cache of which fields are final for a class
   */
  Map<Class<?>, Map<String, Boolean>> fieldCache = new HashMap<>();

  public IgnoreFinalFieldsListener(
      ExecutionEventListener delegate) {
    super(delegate);
  }

  @Override
  public void beforeGetField(Object owner, String fieldName, String className, String methodName,
                             int lineNumber) {
    //Ignore final fields
    if(isFinal(owner, fieldName)) {
      return;
    }

    super.beforeGetField(owner, fieldName, className, methodName, lineNumber);
  }

  private boolean isFinal(Object owner, String fieldName) {
    Class<?> fieldClass = owner instanceof Class<?> ? (Class<?>) owner : owner.getClass();

    Map<String, Boolean> classCache = fieldCache.computeIfAbsent(fieldClass, this::getClassFields);
    return classCache.getOrDefault(fieldName, Boolean.FALSE);
  }

  private Map<String, Boolean> getClassFields(Class<?> aClass) {
    Map<String, Boolean> fieldMap = new HashMap<>();
    while(aClass != null) {
      Field[] fields = aClass.getDeclaredFields();
      Arrays.stream(fields)
          .forEach(field -> fieldMap.put(field.getName(), isFinal(field)));
      aClass = aClass.getSuperclass();
    }

    return fieldMap;
  }

  private Boolean isFinal(Field field) {
    return Modifier.isFinal(field.getModifiers());
  }
}
