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

package com.github.upthewaterspout.fates.core.threading.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.Thread.State;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.github.upthewaterspout.fates.core.states.Decider;
import com.github.upthewaterspout.fates.core.states.StateExplorer;
import com.github.upthewaterspout.fates.core.states.explorers.depthfirst.DepthFirstExplorer;
import com.github.upthewaterspout.fates.core.threading.instrument.monitor.MonitorControl;
import org.junit.Test;


public class ThreadSchedulingListenerTest {

  @Test(timeout = 30000)
  public void yieldWithOneThreadShouldContinueExecution() {
    Decider decider = new DepthFirstExplorer();
    ThreadSchedulingListener scheduler = new ThreadSchedulingListener(decider);
    scheduler.begin();
    scheduler.yield();
  }

  @Test(timeout = 30000)
  public void yieldWithCurrentlyRunningThreadShouldBlock() throws InterruptedException {
    Decider decider = new DepthFirstExplorer();
    ThreadSchedulingListener scheduler = new ThreadSchedulingListener(decider);
    scheduler.begin();

    Thread parentThread = Thread.currentThread();
    Thread newThread = new Thread() {

      public void run() {
        Threads.waitUntilState(parentThread, EnumSet.of(State.WAITING));
        scheduler.beforeThreadExit();
      }
    };
    scheduler.beforeThreadStart(newThread);
    newThread.start();
    scheduler.yield();
    newThread.join();
  }

  @Test(timeout = 30000)
  public void notifyShouldWakeupThreadInWait() throws InterruptedException {
    Decider decider = new DepthFirstExplorer();
    ThreadSchedulingListener
        scheduler = new ThreadSchedulingListener(decider, mock(MonitorControl.class));
    scheduler.begin();

    final Object sync = new Object();
    Thread parentThread = Thread.currentThread();
    Thread newThread = new Thread() {

      public void run() {
        Threads.waitUntilState(parentThread, EnumSet.of(State.WAITING));
        scheduler.beforeSynchronization(sync);
        try {
          scheduler.replaceWait(null, sync, 0, 0);
        } catch (InterruptedException e) {
          //ignore
        }
        scheduler.afterSynchronization(sync);
        scheduler.beforeThreadExit();
      }
    };
    scheduler.beforeThreadStart(newThread);
    newThread.start();
    scheduler.yield();
    Thread.sleep(100);
    assertTrue(newThread.isAlive());
    scheduler.beforeSynchronization(sync);
    scheduler.replaceNotifyAll(null, sync);
    scheduler.afterSynchronization(sync);
    newThread.join();
  }

  @Test(timeout = 300000)
  public void shouldTestAllCombinationsWithThreeBlockingThreads() throws InterruptedException {
    DepthFirstExplorer decider = new DepthFirstExplorer();

    int iterations = 0;
    while(!decider.isCompletelyTested()) {
      iterations++;
      ThreadSchedulingListener scheduler = new ThreadSchedulingListener(decider);
      scheduler.begin();

      Thread t1 = startThread(scheduler, "T1", () -> {
        scheduler.yield();
        scheduler.beforeThreadExit();
      });
      Thread t2 = startThread(scheduler, "T2", () -> {
        scheduler.yield();
        scheduler.beforeThreadExit();
      });

      scheduler.yield();
      decider.done();
    }

    //This is 5 permutations, not 6, because the permutation where the main thread
    //goes doesn't block on the yield doesn't care whether t1 or t2 blocks first.
    assertEquals(5, iterations);
  }

  @Test(timeout=30000)
  public void unparkShouldUnblockParkedThread() throws InterruptedException {
    Decider decider = new DepthFirstExplorer();

    ThreadSchedulingListener scheduler = new ThreadSchedulingListener(decider);
    scheduler.begin();

    Thread t1 = startThread(scheduler, "T1", () -> {
      scheduler.replacePark(null, null );
    });

    Threads.waitUntilState(t1, EnumSet.of(State.WAITING));
    scheduler.replaceUnpark(null, t1);
    scheduler.beforeThreadExit();
    t1.join();
  }

  @Test(timeout=30000)
  public void threadExitShouldUnblockJoin() throws InterruptedException {
    Decider decider = new DepthFirstExplorer();

    ThreadSchedulingListener scheduler = new ThreadSchedulingListener(decider);
    scheduler.begin();


    final Thread mainThread = Thread.currentThread();

    Thread t1 = startThread(scheduler, "T1", () -> {
      try {
        scheduler.replaceJoin(null, mainThread, 0, 0);
      } catch (InterruptedException e) {
        //ignore
      }
    });

    Threads.waitUntilState(t1, EnumSet.of(State.WAITING));
    scheduler.beforeThreadExit();
    t1.join();
  }

  @Test(timeout=30000)
  public void joinOnDeadThreadShouldContinue() throws InterruptedException {
    Decider decider = new DepthFirstExplorer();

    ThreadSchedulingListener scheduler = new ThreadSchedulingListener(decider);
    scheduler.begin();

    Thread t1 = startThread(scheduler, "T1", () -> {
      scheduler.beforeThreadExit();
    });

    scheduler.replaceJoin(null, t1, 0, 0);
    t1.join();
  }

  @Test(timeout=30000)
  public void interruptShouldBreakOutOfJoin() throws InterruptedException {
    Decider decider = new DepthFirstExplorer();

    ThreadSchedulingListener scheduler = new ThreadSchedulingListener(decider);
    scheduler.begin();

    AtomicBoolean interrupted = new AtomicBoolean();
    Thread t1 = startThread(scheduler, "T1", () -> {
      try {
        scheduler.replaceJoin(null, Thread.currentThread(), 0, 0);
      } catch (InterruptedException e) {
        interrupted.set(true);
      }
      scheduler.beforeThreadExit();
    });
    Threads.waitUntilState(t1, EnumSet.of(State.WAITING));
    scheduler.replaceInterrupt(null, t1);
    scheduler.replaceJoin(null, t1, 0, 0);
    assertFalse(scheduler.replaceIsInterrupted(null, t1, false));
    t1.join();
    assertTrue(interrupted.get());
  }

  @Test(timeout=30000)
  public void interruptShouldBreakOutOfPark() throws InterruptedException {
    StateExplorer decider = new DepthFirstExplorer();

    ThreadSchedulingListener scheduler = new ThreadSchedulingListener(decider);
    scheduler.begin();
    AtomicBoolean wasInterrupted = new AtomicBoolean();
    do {
      Thread parker = startThread(scheduler, "T1", () -> {
        scheduler.replacePark(null, null);
        wasInterrupted.set(scheduler.replaceIsInterrupted(null, Thread.currentThread(), false));
        scheduler.beforeThreadExit();
      });
      Thread interrupter = startThread(scheduler, "T1", () -> {
        scheduler.replaceInterrupt(null, parker);
        scheduler.beforeThreadExit();
      });
      scheduler.replaceJoin(null, parker, 0, 0);
      scheduler.replaceJoin(null, interrupter, 0, 0);
      assertTrue(wasInterrupted.get());
      decider.done();
    } while(!decider.isCompletelyTested());
  }

  @Test(timeout=30000)
  public void interruptShouldBreakOutOfWait() throws InterruptedException {
    Decider decider = new DepthFirstExplorer();

    ThreadSchedulingListener scheduler = new ThreadSchedulingListener(decider, mock(MonitorControl.class));
    scheduler.begin();

    AtomicBoolean interrupted = new AtomicBoolean();
    Thread t1 = startThread(scheduler, "T1", () -> {
      scheduler.beforeSynchronization(this);
      try {
        scheduler.replaceWait(null, this, 0, 0);
      } catch (InterruptedException e) {
        interrupted.set(true);
      } finally {
        scheduler.afterSynchronization(this);
      }
      scheduler.beforeThreadExit();
    });
    Threads.waitUntilState(t1, EnumSet.of(State.WAITING));
    scheduler.replaceInterrupt(null, t1);
    scheduler.replaceJoin(null, t1, 0, 0);
    assertFalse(scheduler.replaceIsInterrupted(null, t1, false));
    t1.join();
    assertTrue(interrupted.get());
  }

  @Test(timeout=30000)
  public void monitorShouldBeExclusive() throws InterruptedException {
    DepthFirstExplorer decider = new DepthFirstExplorer();

    final Object sync = new Object();
    while(!decider.isCompletelyTested()) {
      ThreadSchedulingListener scheduler = new ThreadSchedulingListener(decider);
      scheduler.begin();

      AtomicReference<Exception> failure = new AtomicReference<>();
      AtomicBoolean inMonitor = new AtomicBoolean(true);
      scheduler.beforeSynchronization(sync);
      Thread t1 = startThread(scheduler, "T1", () -> {
        scheduler.beforeSynchronization(sync);
        if (inMonitor.get()) {
          failure.set(new Exception("Two threads are holding the monitor!"));
        }
        scheduler.afterSynchronization(sync);
        markCurrentThreadAsDone(scheduler);
      });

      //This yield should do nothing because t1 is blocked
      scheduler.yield();
      inMonitor.set(false);
      scheduler.afterSynchronization(sync);
      markCurrentThreadAsDone(scheduler);
      t1.join();
      if (failure.get() != null) {
        throw new RuntimeException("failed", failure.get());
      }
      decider.done();
    }
  }

  private void markCurrentThreadAsDone(final ThreadSchedulingListener scheduler) {
    try {
      scheduler.beforeThreadExit();
    } catch(IllegalStateException ignore) {

    }
  }

  private Thread startThread(ThreadSchedulingListener scheduler, final String threadName, final Runnable runnable) {
    Thread newThread = new Thread(runnable, threadName);
    scheduler.beforeThreadStart(newThread);
    newThread.start();
    return newThread;
  }

}