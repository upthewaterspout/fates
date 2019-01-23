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

import java.util.function.Supplier;

import com.github.upthewaterspout.fates.core.states.depthfirst.DepthFirstExplorer;

/**
 * A testing harness for running tests many times, exploring the affects of different decisions on
 * the test.
 * <p>
 * Users should construct a {@link RepeatedTest} and pass the test to this harness. The harness is
 * responsible for running the test repeatedly.
 * <p>
 * The algorithm for exploring possible decisions is passed in as the {@link StateExplorer}.
 * Algorithms may explore all possible decisions or some subset of decisions, it is up to the
 * algorithm. Currently the only implementation of {@link StateExplorer} is {@link
 * DepthFirstExplorer}, which explores all possible decisions.
 * <p>
 * This harness assumes that each time the test is run, if the same decision is made twice in a row
 * it will lead to the same test state. If a the explorer makes the same choice twice, but is
 * presented with different results, the test will fail.
 *
 * <p>
 * Example:
 * <pre>
 * {@code
 *   new Fates().explore(decider -> {
 *     int a = * decider.decide(1,3,5);
 *     int b = decider.decide(2,4);
 *     assertIsEven(a * b);
 *   });
 * }
 * </pre>
 */
public class Fates {


  private Supplier<StateExplorer> explorerSupplier = () -> new DepthFirstExplorer();

  /**
   * Configure the {@link StateExplorer} that should be used. The default is {@link DepthFirstExplorer}
   *
   * @param explorerSupplier A supplier for the explorer to use. Each call to {@link #explore(RepeatedTest)}
   * will invoke this supplier to create a new instance of the potentially stateful explorer class.
   */
  public Fates setExplorer(
      Supplier<StateExplorer> explorerSupplier) {
    this.explorerSupplier = explorerSupplier;
    return this;
  }

  /**
   * Execute a test multiple times, exploring some or all of the possible states of the test
   * @param test the test to explore. It will be run many times.
   * @throws Exception if the test fails
   */
  public void explore(RepeatedTest test) throws Exception {
    StateExplorer explorer = explorerSupplier.get();

    int count = 0;
    while(!explorer.isCompletelyTested()) {
      try {
        test.doOnce(explorer);
      } catch(Throwable e) {
        System.err.println(explorer.getTrace());
        throw e;
      }
      explorer.done();

      long estimate = explorer.estimateIterations();
      if(++count % 10 == 0) {
        System.out.println("Tested " + count + "/" + estimate + " iterations");
      }
    }

  }
}
