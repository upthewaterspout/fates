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

public class ClassWithSynchronizedMethodCallsInBranch implements Callable {

  public Object call() {
    if(noArg()) {
      oneArg("x");
    }
    return null;
  }

  public synchronized boolean noArg() {
    return true;
  }

  public synchronized void oneArg(String x) {

  }


}
