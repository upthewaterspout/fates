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

package com.github.upthewaterspout.fates.executor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import com.github.upthewaterspout.fates.core.threading.ThreadFates;

/**
 * A simple parallel executor for use with {@link ThreadFates.MultiThreadedTest}s run
 * inside {@link ThreadFates}. This executor is optimized to avoid extra thread
 * scheduling decisions during it's setup and tear down.
 */
public class ParallelExecutor<OUT> {

  private final Map<String, Callable<OUT>> parallelTasks = new HashMap<>();
  volatile Throwable throwable = null;

  public ParallelExecutor<OUT> inParallel(String label, Callable<OUT> task) {
    this.parallelTasks.put(label, task);
    return this;
  }

  public void run() throws Exception {
    Thread[] threads = new Thread[parallelTasks.size()];

    int threadNum =0;
    for(Map.Entry<String, Callable<OUT>> entry: parallelTasks.entrySet()) {
      String label = entry.getKey();
      Callable<OUT> task = entry.getValue();
      threads[threadNum] = new Thread(label) {
        public void run() {
          try {
            task.call();
          } catch (Throwable t) {
            if(throwable != null) {
              throwable = t;
            }
          }
        }
      };

      threadNum++;
    };

    for(int i =0; i < threads.length; i++) {
      threads[i].start();
    }

    for(int i =0; i < threads.length; i++) {
      threads[i].join();
    }

    if(throwable != null) {
      throw new Exception("Test thread failed", throwable);
    }

  }
}
