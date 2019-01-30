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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.util.Sets;
import org.junit.Test;

public class DepthFirstExplorerJUnitTest {

  @Test
  public void willReturnSecondChoiceAfterFirstIsCompletelyTested() {
    DepthFirstExplorer explorer = new DepthFirstExplorer();

    LinkedHashSet<String> options = new LinkedHashSet<>();
    options.add("one");
    options.add("two");

    String choice = explorer.decide("", options);
    assertEquals("one", choice);
    explorer.done();
    String nextChoice = explorer.decide("", options);
    assertEquals("two", nextChoice);
    explorer.done();
    assertTrue(explorer.isCompletelyTested());
  }

  @Test
  public void willExplore2LevelsOfChoices() {
    DepthFirstExplorer explorer = new DepthFirstExplorer();
    Set<String> options1 = Stream.of("one", "two").collect(Collectors.toSet());
    Set<String> options2 = Stream.of("three", "four").collect(Collectors.toSet());

    explorer.decide("", options1);
    explorer.decide("", options2);
    explorer.done();
    assertFalse(explorer.isCompletelyTested());

    explorer.decide("", options1);
    explorer.decide("", options2);
    explorer.done();
    assertFalse(explorer.isCompletelyTested());

    explorer.decide("", options1);
    explorer.decide("", options2);
    explorer.done();
    assertFalse(explorer.isCompletelyTested());

    explorer.decide("", options1);
    explorer.decide("", options2);
    explorer.done();

    assertTrue(explorer.isCompletelyTested());
  }

  @Test
  public void wilLGenerateADebuggingTrace() {
    DepthFirstExplorer explorer = new DepthFirstExplorer();

    explorer.decide("label1", Sets.newLinkedHashSet(1, 2));
    explorer.decide("label2", Sets.newLinkedHashSet(1, 2));
    explorer.decide("label3", Sets.newLinkedHashSet(1, 2));
    explorer.decide("label4", Sets.newLinkedHashSet(1, 2));
    explorer.decide("label5", Sets.newLinkedHashSet(1, 2));
    String trace = explorer.getTrace();
    String expected =  "\n========================================" +
        "\nTest History:" +
        "\n========================================" +
        "\n" + "label1\nlabel2\nlabel3\nlabel4\nlabel5" +
        "\n========================================";
    assertEquals(expected, trace);
  }

}
