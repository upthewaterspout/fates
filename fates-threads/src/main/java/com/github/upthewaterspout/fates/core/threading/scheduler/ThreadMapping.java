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

import java.util.HashMap;
import java.util.Map;

class ThreadMapping {
  /**
   * All threads in this run
   */
  private final Map<Thread, ThreadID> threadtoID = new HashMap<Thread, ThreadID>();
  /**
   * All threads in this run
   */
  private final Map<ThreadID, Thread> idToThread = new HashMap<ThreadID, Thread>();

  public ThreadMapping() {
  }

  ThreadID getThreadID(Thread thread) {
    ThreadID threadID = threadtoID.get(thread);
    if (threadID == null) {
      throw new IllegalStateException("Unable to find thread id for untracked thread " + thread);
    }
    return threadID;
  }

  void newThread(Thread thread, Thread parent) {
    ThreadID threadID = ThreadID.create(thread, threadtoID.get(parent));
    threadtoID.put(thread, threadID);
    idToThread.put(threadID, thread);
  }

  Thread getThread(ThreadID scheduledThreadID) {
    return idToThread.get(scheduledThreadID);
  }

  ThreadID threadTerminated(Thread thread) {
    ThreadID threadId = threadtoID.remove(thread);
    idToThread.remove(threadId);
    return threadId;
  }

  boolean hasThread(Thread thread) {
    return threadtoID.containsKey(thread);
  }
}