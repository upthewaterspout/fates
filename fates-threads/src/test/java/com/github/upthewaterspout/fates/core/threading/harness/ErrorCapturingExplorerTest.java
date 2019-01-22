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

package com.github.upthewaterspout.fates.core.threading.harness;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;

import com.github.upthewaterspout.fates.core.states.StateExplorer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ErrorCapturingExplorerTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void errorsAreCapturedDuringDecide() {
    StateExplorer delegate = mock(StateExplorer.class);
    ErrorCapturingExplorer explorer = new ErrorCapturingExplorer(delegate);

    when(delegate.decide(anyString(),  any())).thenThrow(new IllegalStateException("fail"));

    Set<String> options = Collections.singleton("hello");

    //This should not fail, it should be captured
    assertEquals("hello", explorer.decide("", options));

    expectedException.expect(IllegalStateException.class);

    explorer.done();
  }

}