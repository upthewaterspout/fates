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

package com.github.upthewaterspout.fates.core.threading.event.confinement;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utility for finding all of the objects reachable from a given object.
 */
public class ReachableObjectFinder {

  /**
   * Return a stream of all objects that are reachable from a given object
   * @param  root - The object to start from
   * @param filter - A filter to control which objects are traversed. If this
   * filter returns false, that object will not be returned in the stream, and it's
   * references will not be followed.
   * @return A Stream of reachable objects
   */
  public Stream<Object> stream(Object root, Predicate<Object> filter) {

    return StreamSupport.stream(new ObjectTraversingIterator(root, filter), false);
  }

  /**
   * Add the first level references from root to the unvisited list, if they have not been visited
   */
  private void addReferences(Object root, Predicate<Object> filter,
                            Deque<Object> unvisited) {
    if(root.getClass().isArray()) {
      arrayReferences(root, filter, unvisited);

    } else {
      fieldReferences(root, filter, unvisited);
    }
  }


  /**
   * Find all of the elements of an array through reflection add them to the unvisited list
   */
  private void arrayReferences(Object root, Predicate<Object> filter,
                               Deque<Object> unvisited) {
    Class clazz = root.getClass();
    Class componentType = clazz.getComponentType();
    if(componentType.isPrimitive()) {
      return;
    }

    int length = Array.getLength(root);
    for(int i=0; i < length; i++) {

      Object object = Array.get(root, i);
      if(filter.test(object)) {
        unvisited.add(object);
      }
    }
  }

  /**
   * Find all of the fields of an object and add them to the unvisited list
   */
  private void fieldReferences(Object root, Predicate<Object> filter,
                               Deque<Object> unvisited) {

    Class clazz = root.getClass();


    while(clazz != null) {
      Field[] fields = clazz.getDeclaredFields();
      Stream.of(fields)
          .filter(this::isObject)
          .filter(this::isNotStatic)
          .map(field -> getValue(root, field))
          .filter(Objects::nonNull)
          .filter(filter)
          .forEach(unvisited::add);
      clazz = clazz.getSuperclass();
    };
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

  private class ObjectTraversingIterator implements Spliterator<Object> {
    private final Deque<Object> unvisited = new ArrayDeque<>();
    private final Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Predicate<Object> filter;

    public ObjectTraversingIterator(Object root,
                                    Predicate<Object> filter) {
      if(filter.test(root)) {
        unvisited.add(root);
      }
      this.filter = object -> !visited.contains(object) && filter.test(object);
    }

    @Override
    public boolean tryAdvance(Consumer<? super Object> action) {
      if(unvisited.isEmpty()) {
        return false;
      }

      Object next = unvisited.pop();
      visited.add(next);
      addReferences(next, filter, unvisited);
      action.accept(next);
      return true;
    }

    @Override
    public Spliterator<Object> trySplit() {
      return null;
    }

    @Override
    public long estimateSize() {
      return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
      return IMMUTABLE | DISTINCT | NONNULL;
    }
  }
}
