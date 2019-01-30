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

package com.github.upthewaterspout.fates.core.states.explorers.depthfirst;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import com.github.upthewaterspout.fates.core.states.StateExplorer;
import com.github.upthewaterspout.fates.core.states.tree.DecisionTree;
import com.github.upthewaterspout.fates.core.states.tree.Trees;

/**
 * An implementation of {@link StateExplorer} that uses a depth first search.
 *
 * This explorer will exhaustively investigate all possible choices, starting by varying
 * the very last choice that was made in the test.
 */
public class DepthFirstExplorer implements StateExplorer {

  DecisionTree<Void> initialState = new DecisionTree<>(null, null);
  DecisionTree<?> currentState = initialState;

  @Override
  public <K> K decide(Object label, Set<K> options) {
    currentState.setOptions(label, options);
    currentState = getUnexploredNextState();
    return (K) currentState.getDecision();
  }

  private DecisionTree<?> getUnexploredNextState() {
    Optional<DecisionTree<?>> optional = currentState.getSubTrees().stream()
        .filter(state -> !state.isCompletelyTested())
        .findFirst();
    return optional.orElseThrow(() -> new IllegalStateException());
  }

  @Override
  public void done() {
    currentState.setOptions("", Collections.emptySet());
    currentState = initialState;
  }

  @Override
  public boolean isCompletelyTested() {
    return initialState.isCompletelyTested();
  }

  @Override
  public long estimateIterations() {
    return Trees.estimateSize(initialState);
  }

  @Override
  public String getTrace() {
    return Trees.showHistory(currentState);
  }
}
