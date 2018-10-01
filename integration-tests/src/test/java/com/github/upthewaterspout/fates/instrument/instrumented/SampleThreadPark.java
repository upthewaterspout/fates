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

package com.github.upthewaterspout.fates.instrument.instrumented;

import java.util.concurrent.locks.LockSupport;

public class SampleThreadPark {

  private Thread thread;

  public void call() throws InterruptedException {
    thread = new Thread() {
      public void run() {
        LockSupport.park();
      }
    };

    thread.start();

    LockSupport.unpark(thread);
    thread.join();
  }
}
