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

import static org.junit.Assert.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import org.assertj.core.util.Sets;
import org.junit.Test;

public class RandomExplorerJUnitTest {

  @Test
  public void exploresFixedNumberOfIterations() {
    RandomExplorer randomExplorer = new RandomExplorer(2, 0);
    randomExplorer.done();
    assertThat(randomExplorer.isCompletelyTested()).isFalse();
    randomExplorer.done();
    assertThat(randomExplorer.isCompletelyTested()).isTrue();
  }

  @Test
  public void seedReproducesSameChoices() {
    RandomExplorer randomExplorer = new RandomExplorer(2, 10);

    //Do an iteration to make sure we get a new seed
    randomExplorer.decide("something", Sets.newLinkedHashSet(1, 2, 3));
    randomExplorer.done();

    int choice1 = randomExplorer.decide("something", setOf100());
    int choice2 = randomExplorer.decide("something", setOf100());
    long foundSeed = randomExplorer.getSeed();

    assertThat(foundSeed).isNotEqualTo(10);

    //Create a new explorer
    randomExplorer = new RandomExplorer(1, foundSeed);
    assertThat(randomExplorer.decide("something", setOf100())).isEqualTo(choice1);
    assertThat(randomExplorer.decide("something", setOf100())).isEqualTo(choice2);
  }

  private Set<Integer> setOf100() {
    return IntStream.range(0, 100).mapToObj(Integer::valueOf).collect(
        Collectors.toSet());
  }

}