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

package com.github.upthewaterspout.fates.core.threading.event;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;

public class IgnoreFinalFieldsListenerTest {
  private ExecutionEventListener delegate = mock(ExecutionEventListener.class);

  private final int instanceField = 0;
  private static final int staticField = 0;

  private int mutableField = 0;
  private static int mutableStatic = 0;


  @Test
  public void ignoresFinalStaticField() {
    IgnoreFinalFieldsListener listener = new IgnoreFinalFieldsListener(delegate);
    listener.beforeGetField(IgnoreFinalFieldsListenerTest.class, "staticField", "any", "any", 0);
    verifyNoMoreInteractions(delegate);
  }

  @Test
  public void ignoresFinalInstanceField() {
    IgnoreFinalFieldsListener listener = new IgnoreFinalFieldsListener(delegate);
    listener.beforeGetField(this, "instanceField", "any", "any", 0);
    verifyNoMoreInteractions(delegate);
  }

  @Test
  public void delegatesMutableField() {
    IgnoreFinalFieldsListener listener = new IgnoreFinalFieldsListener(delegate);
    listener.beforeGetField(this, "mutableField", "any", "any", 0);
    verify(delegate).beforeGetField(any(), any(), any(), any(), anyInt());
  }

  @Test
  public void delegatesStaticMutableField() {
    IgnoreFinalFieldsListener listener = new IgnoreFinalFieldsListener(delegate);
    listener.beforeGetField(IgnoreFinalFieldsListenerTest.class, "mutableStatic", "any", "any", 0);
    verify(delegate).beforeGetField(any(), any(), any(), any(), anyInt());
  }


}