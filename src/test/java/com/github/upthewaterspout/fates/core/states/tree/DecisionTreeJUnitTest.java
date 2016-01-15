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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class DecisionTreeJUnitTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void initialStateReturnsFalseForCompletedTested() {
    DecisionTree<String> state = new DecisionTree<>(null, null);
    assertFalse(state.isCompletelyTested());
  }

  @Test
  public void initialStateMarkedDoneReturnsTrueForCompletedTested() {
    DecisionTree<String> tree = new DecisionTree<>(null, null);
    tree.setOptions(Collections.emptySet());
    assertTrue(tree.isCompletelyTested());
  }

  @Test
  public void singleChildMarkedDoneWillMarkParentAsTested() {
    DecisionTree<String> tree = new DecisionTree<>(null, null);
    Set<String> options = Collections.singleton("one");
    tree.setOptions(options);
    DecisionTree subTree = tree.getSubTrees().iterator().next();
    assertEquals("one", subTree.getDecision());
    subTree.setOptions(Collections.emptySet());
    assertTrue(subTree.isCompletelyTested());
    assertTrue(tree.isCompletelyTested());
  }

  @Test
  public void throwsIllegalStateExceptionWhenGivenOptionsForAtestedState() {
    DecisionTree<String> tree = new DecisionTree<>(null, null);
    Set<String> options = Collections.singleton("one");
    tree.setOptions(options);
    DecisionTree subTree = tree.getSubTrees().iterator().next();
    subTree.setOptions(Collections.emptySet());
    assertTrue(tree.isCompletelyTested());
    thrown.expect(IllegalStateException.class);
    tree.setOptions(options);
  }


  @Test
  public void willDetectInconsistentChoices() {
    DecisionTree<String> state = new DecisionTree<>(null, null);
    Set<String> options1 = Stream.of("one", "two").collect(Collectors.toSet());
    state.setOptions(options1);
    Set<String> options2 = Stream.of("threee", "four").collect(Collectors.toSet());
    thrown.expect(IllegalStateException.class);
    state.setOptions(options2);
  }

}