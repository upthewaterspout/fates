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
import java.util.List;
import java.util.concurrent.*;

import com.github.upthewaterspout.fates.core.threading.Fates;
import jdk.nashorn.internal.codegen.CompilerConstants;

/**
 * Some syntax sugar around {@link Fates} that makes it easier to set
 * up a test that uses multiple threads.
 */
public class ParallelExecutor<OUT> {

  private final List<Callable<OUT>> parallelTasks = new ArrayList<>();
  volatile Throwable throwable = null;

  public ParallelExecutor<OUT> inParallel(Callable<OUT> task) {
    this.parallelTasks.add(task);
    return this;
  }

  public void run() throws Exception {
    Thread[] threads = new Thread[parallelTasks.size()];

    for(int i =0; i < threads.length; i++) {
      Callable<OUT> task = parallelTasks.get(i);
      threads[i] = new Thread(() -> {
              try {
                task.call();
              } catch (Throwable t) {
                if(throwable != null) {
                  throwable = t;
                }
              }
            });
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
