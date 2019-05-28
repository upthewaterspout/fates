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

import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
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

  @Test(expected = AssertionError.class)
  public  void exploresUntilFailureIsFound() throws Exception {
    new Fates()
      .explore(decider -> {
        int a = decider.decide("a", new HashSet<>(Arrays.asList(1,2,3,4,5)));
        int b = decider.decide("b", new HashSet<>(Arrays.asList(5,4,3,2,1)));
        assertNotEquals(a, b);
      });
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