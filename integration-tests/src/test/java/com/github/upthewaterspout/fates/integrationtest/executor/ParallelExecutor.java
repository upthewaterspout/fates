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

package com.github.upthewaterspout.fates.integrationtest.executor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import com.github.upthewaterspout.fates.core.threading.Fates;

/**
 * Some syntax sugar around {@link Fates} that makes it easier to set
 * up a test that uses multiple threads.
 */
public class ParallelExecutor<OUT> {

  private final List<Callable<OUT>> parallelTasks = new ArrayList<>();

  public ParallelExecutor<OUT> inParallel(Callable<OUT> task) {
    this.parallelTasks.add(task);
    return this;
  }

  public void run() throws Exception {
    List<OUT> results = Collections.synchronizedList(new ArrayList<>());
    List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
    List<Thread> threads = new ArrayList<>();

    for(Callable<OUT> task : parallelTasks) {
      threads.add(new Thread(() -> {
        try {
          results.add(task.call());
        } catch (Throwable t) {
          exceptions.add(t);
        }
      }));
    };

    threads.forEach(Thread::start);
    for(Thread thread : threads) {
      thread.join();
    }

    if(!exceptions.isEmpty()) {
      throw new Exception("Test thread failed", exceptions.get(0));
    }

  }
}
