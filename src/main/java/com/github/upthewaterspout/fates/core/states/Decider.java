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

import java.util.Set;

/**
 * Interface for making a choice between a set of possible options
 *
 * See {@link StateExplorationHarness} for how to use this interface.
 */
public interface Decider {
  /**
   * User code should call this method to indicate that there is another choice to be made
   * among the possible options. The decider will pick one of the options and return it.
   * @param <K> The type of the possible choices
   * @param options The possible choices
   * @return The decision
   */
  <K> K decide(Set<K> options);
}
