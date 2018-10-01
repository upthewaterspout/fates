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

import com.github.upthewaterspout.fates.core.states.tree.DecisionTree;

/**
 * An algorithm for exploring the space of possible decisions during a test run.
 * <p>
 * The {@link StateExplorationHarness} will take this algorithm and use it as the
 * {@link Decider} when executing a test. After the test is done, the {@link StateExplorationHarness}
 * will call the done() method. The harness will then execute the test <i>again</i>, and the
 * {@link StateExplorer} is expected to make new decisions the next time around.
 *
 * <p>
 * The harness will continue to execute the test until {@link #isCompletelyTested()} returns true.
 *
 * <p>
 * It is helpful to implement this {@link #estimateIterations()} method to estimate the remaining
 * number of tests runs, but it is not required.
 *
 * <p>
 * See {@link DecisionTree} for a utility for keeping track of previously made decisions.
 *
 */
public interface StateExplorer extends Decider {
  /**
   * Indicate to the explorer that we have finished a complete test
   * run. The explorer should reset itself to the initial state, making different
   * choices on the next time through.
   */
  void done();

  /**
   * Return true if testing should stop at this point, because all or enough states
   * have been explored.
   * @return true if the test should stop
   */
  boolean isCompletelyTested();

  /**
   * Estimate the number of iterations of the test that need to be performed
   * @return estimated number of iterations
   */
  long estimateIterations();

}
