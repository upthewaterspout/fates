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

import java.lang.Thread.State;
import java.util.concurrent.Callable;

public class SampleWaitNotify implements Callable {


  @Override public Object call() throws Exception {
    Thread thread = new Thread() {
      public void run () {
        synchronized(this) {
          try {
            this.wait();
          }
          catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    };
    thread.start();
    //Wait for the thread do be waiting
    while(!thread.getState().equals(State.WAITING)) {
      Thread.sleep(10);
    }
    synchronized(thread) {
      thread.notify();
    }
    thread.join();

    return null;
  }
}
