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

package com.github.upthewaterspout.fates.core.threading.confinement;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Utility for finding all of the objects reachable from a given object.
 */
public class ReachableObjectFinder {

  /**
   * Return a stream of all objects that are reachable from a given object
   */
  public Stream<Object> stream(Object root) {
    return stream(root, Collections.newSetFromMap(new IdentityHashMap<>()));
  }

  public Stream<Object> stream(Object root, Set<Object> visited) {
    return root.getClass().isArray() ? streamArray(root, visited) : streamObject(root, visited);
  }


  /**
   * Stream over the element in a array
   */
  private Stream<Object> streamArray(Object root, Set<Object> visited) {
    Class clazz = root.getClass();
    Class componentType = clazz.getComponentType();
    if(componentType.isPrimitive()) {
      //If this is a primitive array, just include the array itself
      return Stream.of(root);
    }

    //Otherwise, stream all of the elements in the array
    Stream.Builder<Object> builder = Stream.builder();
    int length = Array.getLength(root);
    for(int i=0; i < length; i++) {
      builder.add(Array.get(root, i));
    }
    Stream result = builder.build()
        .flatMap(object -> stream(object, visited));

    return Stream.concat(Stream.of(root), result);
  }

  private Stream<Object> streamObject(Object root, Set<Object> visited) {
    visited.add(root);

    Stream result = Stream.of(root);
    Class clazz = root.getClass();

    while(clazz != null) {
      Field[] fields = clazz.getDeclaredFields();
      Stream<Object> fieldValues = Stream.of(fields)
          .filter(this::isObject)
          .filter(this::isNotStatic)
          .map(field -> getValue(root, field))
          .filter(Objects::nonNull)
          .filter(object -> !visited.contains(object))
          .flatMap(object -> stream(object, visited));
      result = Stream.concat(result, fieldValues);
      clazz = clazz.getSuperclass();
    };

    return result;
  }

  private Object getValue(Object root, Field field) {
    field.setAccessible(true);
    try {
      return field.get(root);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean isNotStatic(Field field) {
    return !Modifier.isStatic(field.getModifiers());
  }

  private boolean isObject(Field field) {
    return !field.getType().isPrimitive();
  }

}
