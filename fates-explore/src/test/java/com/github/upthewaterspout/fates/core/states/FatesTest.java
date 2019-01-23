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

package com.github.upthewaterspout.fates.core.states;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Set;

import org.junit.Test;

public class FatesTest {

  @Test
  public  void exploresUntilDone() throws Exception {
    RepeatedTest test = mock(RepeatedTest.class);
    NIteratorExplorer explorer = new NIteratorExplorer(3);
    new Fates()
        .setExplorer(() -> explorer)
        .explore(test);

    verify(test, times(3)).doOnce(eq(explorer));
  }


  /**
   * A test state explorer that tells the harness to do the test N times.
   */
  private class NIteratorExplorer implements StateExplorer {
    private int count;

    public NIteratorExplorer(int count) {
      this.count = count;
    }


    @Override
    public void done() {
      count--;

    }

    @Override
    public boolean isCompletelyTested() {
      return count == 0;
    }

    @Override
    public long estimateIterations() {
      return count;
    }

    @Override
    public String getTrace() {
      return null;
    }

    @Override
    public <K> K decide(Object label, Set<K> options) {
      return null;
    }
  }
}