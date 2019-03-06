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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;

import com.github.upthewaterspout.fates.core.threading.ThreadFates;
import com.github.upthewaterspout.fates.executor.ParallelExecutor;
import org.junit.Test;

/**
 * Integration test that {@link ThreadFates} will correctly handle an interrupt
 */
public class ThreadInterruptTest {

  @Test()
  public void interruptOfObjectWaitShouldSucceed() throws Throwable {
    new ThreadFates().run(() -> {
      Waiter waiter = new Waiter();

      Thread toInterrupt = new Thread(waiter::doWait);
      Thread interrupter = new Thread(() -> toInterrupt.interrupt());
      toInterrupt.start();
      interrupter.start();

      toInterrupt.join();
      interrupter.join();

      assertEquals(true, waiter.interrupted);
    });
  }

  @Test()
  public void interruptOfParkedThreadShouldSucceed() throws Throwable {
    new ThreadFates().setTrace(true).run(() -> {
      Parker parker = new Parker();

      Thread toInterrupt = new Thread(parker::doPark);
      Thread interrupter = new Thread(() -> toInterrupt.interrupt());
      toInterrupt.start();
      interrupter.start();

      toInterrupt.join();
      interrupter.join();

      assertEquals(true, parker.interrupted);
    });
  }

  private static class Waiter {
    boolean interrupted = false;

    public synchronized boolean doWait() {
      while(true) {
        try {
          this.wait();
        } catch (InterruptedException e) {
          this.interrupted = true;
          break;
        }
      }
      return interrupted;
    }
  }

  private static class Parker {
    boolean interrupted = false;
    public synchronized boolean doPark() {
      try {
        new CountDownLatch(1).await();
      } catch (InterruptedException e) {
        this.interrupted = true;
      }
      return interrupted;
    }
  }
}