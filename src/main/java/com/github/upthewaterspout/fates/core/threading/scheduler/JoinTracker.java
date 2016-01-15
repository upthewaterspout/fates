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

import java.util.*;

/**
 * Keeps track of what threads are calling join on other threads. Used to
 * know what threads can be unblocked when a thread exits.
 */
public class JoinTracker {
  Map<Thread, List<Thread>> joins = new HashMap<>();

  public void join(Thread joiner, Thread joinee) {
    List<Thread> joiners = joins.get(joinee);
    if(joiners == null) {
      joiners = new ArrayList<>();
      joins.put(joinee, joiners);
    }

    joiners.add(joiner);
  }

  public Collection<Thread> threadTerminated(Thread thread) {
    List<Thread> result = joins.remove(thread);

    return result == null ? Collections.emptyList() : result;
  }

}
