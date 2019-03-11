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

package com.github.upthewaterspout.fates.core.states.explorers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.github.upthewaterspout.fates.core.states.StateExplorer;

/**
 * A state explorer that makes choices randomly, for a fixed number of maxIterations.
 *
 * This explorer does not keep track of previously explored states at all, so it will
 * not notice if it is repeating a given state.
 *
 * The {@link #getTrace()} method will return a trace that includes
 * a random seed. Providing that seed to the {@link RandomExplorer#RandomExplorer(int, long)}
 * will initialize the explorer with a random seed that will lead to the same trace (assuming
 * no code changes). This is helpful when trying to debug a failure. Using the provided random seed
 * will cause this explorer to reproduce the exact same conditions on the *first* iteration.
 */
public class RandomExplorer implements StateExplorer {

  /**
   * The maximum number of iterations
   */
  private final int maxIterations;

  private final Random random;

  /**
   * The current iteration
   */
  private int iteration;

  /**
   * The random seed that started the current iteration
   */
  private long seed;

  /**
   * A history of decisions made by the current iteration, used for
   * the debugging trace
   */
  private final Collection<Object> history = new ArrayList<>();

  public RandomExplorer(int iterations, long seed) {
    this.maxIterations = iterations;
    this.seed = seed;
    this.random = new Random(seed);
  }


  @Override
  public void done() {
    iteration++;
    seed = random.nextLong();
    random.setSeed(seed);
    history.clear();
  }

  @Override
  public boolean isCompletelyTested() {
    return iteration >= maxIterations;
  }

  @Override
  public long estimateIterations() {
    return maxIterations;
  }

  @Override
  public String getTrace() {
    StringBuilder trace = new StringBuilder("Random Seed: " + seed + "\n");
    if(history.size() < 100) {
      history.stream().forEach(line -> trace.append(line).append("\n"));
    }

    return trace.toString();
  }

  public long getSeed() {
    return seed;
  }

  @Override
  public <K> K decide(Object label, Set<K> options) {
    history.add(label);
    int choice = random.nextInt(options.size());

    return options.stream()
        .skip(choice)
        .findFirst().orElseThrow(IllegalStateException::new);
  }
}
