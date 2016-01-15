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

import com.github.upthewaterspout.fates.core.states.depthfirst.DepthFirstExplorer;

/**
 * A testing harness for running tests many times, exploring the affects of different decisions on
 * the test.
 *
 * Users should construct a {@link RepeatedTest} and pass the test to this harness. The harness is
 * responsible for running the test repeatedly.
 *
 * The algorithm for exploring possible decisions is passed in as the {@link StateExplorer}.
 * Algorithms may explore all possible decisions or some subset of decisions, it is up to the
 * algorithm. Currently the only implementation of {@link StateExplorer} is {@link
 * DepthFirstExplorer}, which explores all possible decisions.
 *
 * This harness assumes that each time the test is run, if the same decision is made twice in a row
 * it will lead to the same test state. If a the explorer makes the same choice twice, but is
 * presented with different results, the test will fail.
 *
 * Example
 * <code>
 * StateExplorationHarness.explore(new DepthFirstExplorer(), decider -> { int a =
 * decider.decide(1,3,5); int b = decider.decide(2,4); assertIsEven(a * b); });
 * </code>
 */
public class StateExplorationHarness {

  /**
   * Execute a test multiple times, exploring some or all of the possible states of the test
   * @param explorer An state space exploration strategy to use.
   * @param test the test to explore. It will be run many times.
   */
  public static void explore(StateExplorer explorer, RepeatedTest test) throws Exception {
    int count = 0;
    while(!explorer.isCompletelyTested()) {
      test.doOnce(explorer);
      explorer.done();

      long estimate = explorer.estimateIterations();
      if(++count % 10 == 0) {
        System.out.println("Tested " + count + "/" + estimate + " iterations");
      }
    }

  }
}
