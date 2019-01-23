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

/**
 * A test which contains decision points and should be run many times to explore different possible
 * decisions and their affects.
 *
 * Users should implement this interface for their tests and pass the test to {@link
 * Fates} {@link Fates} will execute the test repeatedly until
 * all possible decisions have been exercised.
 */
public interface RepeatedTest {

  /**
   * Execute a test, which has some possible decisions to be made during the test. The test
   * should use the passed in {@link Decider} to choose among possible options at each decision point
   *
   * @param decider The decider that the test should use to make choices during the test execution
   * @throws Exception if the test fails
   */
  void doOnce(Decider decider) throws Exception;
}
