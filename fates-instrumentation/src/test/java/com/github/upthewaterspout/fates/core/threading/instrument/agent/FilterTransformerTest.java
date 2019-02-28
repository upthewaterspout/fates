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

package com.github.upthewaterspout.fates.core.threading.instrument.agent;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;

import org.junit.Test;

public class FilterTransformerTest {

  @Test
  public void transformHonorsExcludes() throws IllegalClassFormatException {

    ClassFileTransformer delegate = mock(ClassFileTransformer.class);
    FilterTransformer filterTransformer = new FilterTransformer(delegate, "excluded");
    byte[] transfomed = new byte[0];
    when(delegate.transform(any(), any(), any() ,any() ,any())).thenReturn(transfomed);

    assertEquals(transfomed, filterTransformer.transform(null, "hello world", null, null, null));
    assertEquals(null, filterTransformer.transform(null, "excluded hello world", null, null, null));
    assertEquals(null, filterTransformer.transform(null, "excluded", null, null, null));
  }

}