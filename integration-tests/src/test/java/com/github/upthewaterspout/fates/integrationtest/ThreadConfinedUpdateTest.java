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

package com.github.upthewaterspout.fates.integrationtest;

import java.util.HashSet;
import java.util.Set;

import com.github.upthewaterspout.fates.executor.ParallelExecutor;
import com.github.upthewaterspout.fates.core.threading.ThreadFates;
import org.junit.Test;

public class ThreadConfinedUpdateTest {

  @Test(timeout =  300_000L)
  public void shouldNotHangForThreadConfinedUpdates() throws Throwable {
    new ThreadFates().run(() -> {
      new ParallelExecutor<Set<Integer>>()
          .inParallel("updater1", () -> new ThreadConfinedChanges().update())
          .inParallel("updater2", () -> new ThreadConfinedChanges().update())
          .run();

    });
  }

  private static class ThreadConfinedChanges {

    public Set<Integer> update() {
      Set<Integer> set = new HashSet<>();

      for(int i =0; i < 1000; i++) {
        set.add(new Integer(i));
      }

      return set;
    }

  }

}