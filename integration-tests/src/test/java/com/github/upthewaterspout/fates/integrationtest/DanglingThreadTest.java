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

import java.util.concurrent.locks.LockSupport;

import com.github.upthewaterspout.fates.core.threading.ThreadFates;
import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Integration test that {@link ThreadFates} will fail a test that launches
 * threads during the test but doesn't clean them up and join them at the end.
 */
public class DanglingThreadTest {


  @Test()
  public void shouldDetectDanglingBlockedThread() throws Throwable {

    Assertions.assertThatThrownBy(() -> {
      new ThreadFates().run(() -> {
        Thread thread = new Thread(DanglingThreadTest::joinCurrentThread);
        thread.start();
      });
    }).isInstanceOf(IllegalStateException.class);
  }

  @Test()
  public void shouldWaitForAnyUnfinishedThreadsToComplete() throws Throwable {

    new ThreadFates().run(() -> {
      Thread thread = new Thread(() -> {
        LockSupport.parkNanos(10);
      });
      thread.start();
    });
  }

  private static void joinCurrentThread() {
    try {
      Thread.currentThread().join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}