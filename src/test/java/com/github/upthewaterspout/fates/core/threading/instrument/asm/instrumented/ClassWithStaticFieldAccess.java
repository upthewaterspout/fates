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

package com.github.upthewaterspout.fates.core.threading.instrument.asm.instrumented;

import java.util.concurrent.Callable;

import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventSingleton;


public class ClassWithStaticFieldAccess implements Callable {
  static int a = 5;

  public Object call() {

    ExecutionEventSingleton.beforeSetField(ClassWithStaticFieldAccess.class, null, "string", "String", 33);
    a = a + 1;
    return a;
  }
}
