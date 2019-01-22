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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ReachableObjectFinderTest {

  @Test
  public void findsReachableObjects() {

    ObjectWithReferences root = new ObjectWithReferences();
    ObjectWithReferences a = new ObjectWithReferences();
    ObjectWithReferences b = new ObjectWithReferences();
    root.left = a;
    a.right = b;

    assertThat(new ReachableObjectFinder().stream(root, object -> true)).contains(root, a, b);
  }

  @Test
  public void detectsCyclesInGraph() {
    ObjectWithReferences root = new ObjectWithReferences();
    ObjectWithReferences a = new ObjectWithReferences();
    ObjectWithReferences b = new ObjectWithReferences();
    root.left = a;
    a.left = b;
    b.right = root;

    assertThat(new ReachableObjectFinder().stream(root, object -> true)).contains(root, a, b);
  }

  @Test
  public void findsReachableObjectsThroughArrays() {
    ObjectWithReferences root = new ObjectWithReferences();
    ObjectWithReferences a = new ObjectWithReferences();
    ObjectWithReferences c = new ObjectWithReferences();
    Object[] b = new Object[] {a};
    root.right = b;
    a.left = c;

    assertThat(new ReachableObjectFinder().stream(root, object -> true)).contains(root, a, b, c);
  }


  @Test
  public void filterStopsReferenceChasing() {
    ObjectWithReferences root = new ObjectWithReferences();
    ObjectWithReferences a = new ObjectWithReferences();
    ObjectWithReferences b = new ObjectWithReferences();
    root.left = a;
    a.left = b;

    assertThat(new ReachableObjectFinder().stream(root, object -> !object.equals(a))).contains(root);
  }

  public static class ObjectWithReferences {
    private Object left;
    private Object right;
  }

}