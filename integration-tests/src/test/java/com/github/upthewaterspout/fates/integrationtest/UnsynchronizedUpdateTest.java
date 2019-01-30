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

import static org.junit.Assert.assertEquals;

import com.github.upthewaterspout.fates.executor.ParallelExecutor;
import com.github.upthewaterspout.fates.core.threading.ThreadFates;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Integration test that {@link ThreadFates} can correctly catch a race condition
 * between two threads doing an unsynchronized update.
 */
public class UnsynchronizedUpdateTest {
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test()
  public void shouldFailOnUnsynchronizedUpdate() throws Exception {
    expectedException.expect(AssertionError.class);
    new ThreadFates().setTrace(true).run(() -> {
      UnsynchronizedUpdate updater = new UnsynchronizedUpdate();
      new ParallelExecutor<Integer>()
          .inParallel("updater1", updater::update)
          .inParallel("updater2", updater::update)
          .run();

      assertEquals(2, updater.getValue());
    });
  }

  private static class UnsynchronizedUpdate {
    int field = 0;

    public int update() {
      int old = field;
      field = old + 1;
      return field;
    }

    public int getValue() {
      return field;
    }
  }

}