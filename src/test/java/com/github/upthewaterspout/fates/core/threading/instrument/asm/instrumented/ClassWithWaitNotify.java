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

import java.lang.Thread.State;
import java.util.concurrent.Callable;

public class ClassWithWaitNotify implements Callable, Runnable {
  public Object sync = new Object();

  public  Object call() throws InterruptedException {

    Thread thread = new Thread(this);
    thread.start();

    while(!thread.getState().equals(State.WAITING)) {
      Thread.sleep(10);
    }

    synchronized(sync) {
      sync.notify();
    }
    thread.join();

    return sync;
  }

  public void run() {
    synchronized (sync) {
      try {
        sync.wait();
      }
      catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
