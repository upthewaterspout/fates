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

import java.util.Objects;

/**
 * A unique identifier for a thread. This thread id is designed
 * such that it will be consistent between runs of a test. The uniquely
 * identifying part of the id consists of the parent and childNumber.
 * A thread is therefore identified by what thread started it, and how
 * many other threads were started by that thread previously.
 *
 * The full thread id is therefore a tree
 * Eg
 * Thread A starts Thread B followed by C.
 * Thread C starts thread D
 *
 * The tree is then
 *
 *  A
 * / \
 * B  C
 * \
 * D
 *
 * This ensures repeatability of thread ids between runs of the test,
 * whereas the thread name may change between runs.
 */
public class ThreadID {
  private int childCount;
  private final ThreadID parent;
  private final int childNumber;
  private final String name;

  private ThreadID(String name, ThreadID parent, int childNumber) {
    this.childNumber = childNumber;
    this.parent = parent;
    this.name = name;
  }

  public static ThreadID create(Thread thread, ThreadID parent) {
    int childNumber = parent != null ? parent.childCount++ : 0;
    return new ThreadID(thread.getName(), parent, childNumber);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ThreadID threadID = (ThreadID) o;
    return childNumber == threadID.childNumber &&
        Objects.equals(parent, threadID.parent);
  }

  @Override
  public int hashCode() {
    return Objects.hash(parent, childNumber);
  }

  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append(name).append("[");
    result.append("ThreadID[");
    for(ThreadID id = this; id != null; id = id.parent) {
      result.append(id.childNumber).append(",");
    }
    result.append("]");
    return result.toString();
  }


}
