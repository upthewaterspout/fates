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

package com.github.upthewaterspout.fates.integrationtest;

import static org.junit.Assert.assertEquals;

import com.github.upthewaterspout.fates.core.threading.ThreadFates;
import com.github.upthewaterspout.fates.executor.ParallelExecutor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class WaitNotifyTest {
  @Test()
  public void shouldPassWithCorrectWaitNotify() throws Throwable {
    new ThreadFates().run(() -> {

      WaitNotify waitNotify = new WaitNotify();
      new ParallelExecutor<Boolean>()
          .inParallel("waiter", waitNotify::doWait)
          .inParallel("notifier", waitNotify::doNotify)
          .run();

      assertEquals(true, waitNotify.notified);
    });
  }

  private static class WaitNotify {
    boolean notified = false;

    public synchronized boolean doWait() {
      while(!notified) {
        try {
          this.wait();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
      return notified;
    }

    public synchronized Boolean doNotify() {
      this.notified = true;
      this.notifyAll();

      return true;
    }

  }

}