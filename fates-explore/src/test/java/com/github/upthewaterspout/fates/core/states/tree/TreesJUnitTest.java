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

package com.github.upthewaterspout.fates.core.states.tree;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

public class TreesJUnitTest {

  @Test
  public void givesCorrectEstimatesWithTwoLevelsOfChoices() {
    DecisionTree<String> state = new DecisionTree<>(null, null);
    Set<String> options1 = Stream.of("one", "two").collect(Collectors.toSet());
    Set<String> options2 = Stream.of("three", "four").collect(Collectors.toSet());
    state.setOptions("", options1);
    state.getSubTrees().iterator().next().setOptions("", options2);
    state.getSubTrees().iterator().next().getSubTrees().iterator().next().setOptions("", Collections.emptySet());
    assertEquals(4, Trees.estimateSize(state));
  }

}
