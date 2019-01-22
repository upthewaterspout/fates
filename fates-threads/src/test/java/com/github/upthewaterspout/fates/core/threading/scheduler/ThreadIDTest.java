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

package com.github.upthewaterspout.fates.core.threading.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;


public class ThreadIDTest {

  @Test
  public void testEqualsWithNoParent() {
    ThreadID threadID = ThreadID.create(Thread.currentThread(), null);
    ThreadID threadID2 = ThreadID.create(Thread.currentThread(), null);
    threadID.equals(threadID2);
  }

  @Test
  public void testTwoChildThreadsAreNotEqualWithSameParent() {
    ThreadID parent = ThreadID.create(Thread.currentThread(), null);
    ThreadID id1 = ThreadID.create(new Thread(), parent);
    ThreadID id2 = ThreadID.create(new Thread(), parent);
    assertNotEquals(id1,id2);
  }

  @Test
  public void testTwoChildThreadsAreEqualIfParentsAreDifferentInstances() {
    ThreadID parent1 = ThreadID.create(Thread.currentThread(), null);
    ThreadID parent2 = ThreadID.create(Thread.currentThread(), null);
    ThreadID child1 = ThreadID.create(new Thread(), parent1);
    ThreadID child2 = ThreadID.create(new Thread(), parent2);
    assertEquals(child1, child2);
  }

}