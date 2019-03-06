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

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class SynchronizationTrackerTest {
  private SynchronizationTracker<Thread> tracker = new SynchronizationTracker<Thread>();
  private Thread thread1 = new Thread();
  private Thread thread2 = new Thread();
  private Thread thread3 = new Thread();
  private Object sync1 = new Object();
  private Object sync2 = new Object();

  @Test
  public void shouldBlockOnHeldMonitor() {
    assertEquals(Collections.emptySet(), tracker.monitorEnter(thread1, sync1));
    assertEquals(Collections.singleton(thread2), tracker.monitorEnter(thread2, sync1));
    assertEquals(Collections.singleton(thread2), tracker.monitorExit(thread1, sync1));
    assertEquals(Collections.emptySet(), tracker.threadResumed(thread2));
    assertEquals(Collections.emptySet(), tracker.monitorExit(thread2, sync1));
    assertEquals(Collections.emptyMap(), tracker.getMonitors());
  }

  @Test(expected = IllegalMonitorStateException.class)
  public void exitShouldFailExitingUnheldMonitor() {
    tracker.monitorExit(thread1, sync1);
  }

  @Test(expected = IllegalMonitorStateException.class)
  public void waitShouldFailExitingUnheldMonitor() {
    tracker.wait(thread1, sync1);
  }

  @Test(expected = IllegalMonitorStateException.class)
  public void notifyShouldFailExitingUnheldMonitor() {
    tracker.notify(thread1, sync1);
  }

  @Test(expected = IllegalMonitorStateException.class)
  public void notifyAllShouldFailExitingUnheldMonitor() {
    tracker.notifyAll(thread1, sync1);
  }

  @Test
  public void monitorIsReentrant() {
    assertEquals(Collections.emptySet(), tracker.monitorEnter(thread1, sync1));
    assertEquals(Collections.emptySet(), tracker.monitorEnter(thread1, sync1));
    assertEquals(Collections.singleton(thread2), tracker.monitorEnter(thread2, sync1));
    assertEquals(Collections.emptySet(), tracker.monitorExit(thread1, sync1));
    assertEquals(Collections.singleton(thread2), tracker.monitorExit(thread1, sync1));
    assertEquals(Collections.emptySet(), tracker.threadResumed(thread2));
    assertEquals(Collections.emptySet(), tracker.monitorExit(thread2, sync1));
    assertEquals(Collections.emptyMap(), tracker.getMonitors());
  }

  @Test
  public void monitorsAreIndependent() {
    assertEquals(Collections.emptySet(), tracker.monitorEnter(thread1, sync1));
    assertEquals(Collections.emptySet(), tracker.monitorEnter(thread2, sync2));
    assertEquals(Collections.emptySet(), tracker.monitorExit(thread1, sync1));
    assertEquals(Collections.emptySet(), tracker.monitorExit(thread2, sync2));
    assertEquals(Collections.emptyMap(), tracker.getMonitors());
  }

  @Test
  public void notifyWithNoWaitersDoesNothing() {
    assertEquals(Collections.emptySet(), tracker.monitorEnter(thread1, sync1));
    tracker.notify(thread1, sync1);
    assertEquals(Collections.emptySet(), tracker.monitorExit(thread1, sync1));
    assertEquals(Collections.emptyMap(), tracker.getMonitors());
  }

  @Test
  public void notifyUnblocksOneWaiter() {
    assertEquals(Collections.emptySet(), tracker.monitorEnter(thread1, sync1));
    assertEquals(Collections.emptySet(), tracker.wait(thread1, sync1));
    assertEquals(Collections.emptySet(), tracker.monitorEnter(thread2, sync1));
    assertEquals(Collections.emptySet(), tracker.wait(thread2, sync1));
    assertEquals(Collections.emptySet(), tracker.monitorEnter(thread3, sync1));
    tracker.notify(thread3, sync1);
    assertEquals(Collections.singleton(thread1), tracker.monitorExit(thread3, sync1));
    assertEquals(Collections.emptySet(), tracker.threadResumed(thread1));
    tracker.notify(thread1, sync1);
    assertEquals(Collections.singleton(thread2), tracker.monitorExit(thread1, sync1));
    assertEquals(Collections.emptySet(), tracker.threadResumed(thread2));
    assertEquals(Collections.emptySet(), tracker.monitorExit(thread2, sync1));
    assertEquals(Collections.emptyMap(), tracker.getMonitors());
  }

  @Test
  public void notifyAllUnblocksAllWaiters() {
    assertEquals(Collections.emptySet(), tracker.monitorEnter(thread1, sync1));
    assertEquals(Collections.emptySet(), tracker.wait(thread1, sync1));
    assertEquals(Collections.emptySet(), tracker.monitorEnter(thread2, sync1));
    assertEquals(Collections.emptySet(), tracker.wait(thread2, sync1));
    assertEquals(Collections.emptySet(), tracker.monitorEnter(thread3, sync1));
    tracker.notifyAll(thread3, sync1);
    assertEquals(new HashSet(Arrays.asList(thread1, thread2)), tracker.monitorExit(thread3, sync1));
    assertEquals(Collections.singleton(thread2), tracker.threadResumed(thread1));
    assertEquals(Collections.singleton(thread2), tracker.monitorExit(thread1, sync1));
    assertEquals(Collections.emptySet(), tracker.threadResumed(thread2));
    assertEquals(Collections.emptySet(), tracker.monitorExit(thread2, sync1));
    assertEquals(Collections.emptyMap(), tracker.getMonitors());
  }

  @Test
  public void waiterResumesAtOriginalDepth() {
    assertEquals(Collections.emptySet(), tracker.monitorEnter(thread1, sync1));
    assertEquals(Collections.emptySet(), tracker.monitorEnter(thread1, sync1));
    assertEquals(Collections.emptySet(), tracker.wait(thread1, sync1));
    assertEquals(Collections.emptySet(), tracker.monitorEnter(thread2, sync1));
    tracker.notify(thread2, sync1);
    assertEquals(Collections.singleton(thread1), tracker.monitorExit(thread2, sync1));
    assertEquals(Collections.emptySet(), tracker.threadResumed(thread1));
    assertEquals(Collections.emptySet(), tracker.monitorExit(thread1, sync1));
    assertEquals(Collections.emptySet(), tracker.monitorExit(thread1, sync1));
    assertEquals(Collections.emptyMap(), tracker.getMonitors());
  }

  @Test
  public void interruptWakesUpThreadIfMonitorIsNotHeldByOtherThread() {
    assertEquals(Collections.emptySet(), tracker.monitorEnter(thread1, sync1));
    assertEquals(Collections.emptySet(), tracker.monitorEnter(thread1, sync1));
    assertEquals(Collections.emptySet(), tracker.wait(thread1, sync1));

    assertTrue(tracker.isWaitingForNotify(thread1, sync1));
    assertFalse(tracker.isBlockedOnMonitor(thread1, sync1));

    assertFalse(tracker.interrupt(thread1));

    assertFalse(tracker.isWaitingForNotify(thread1, sync1));
    assertFalse(tracker.isBlockedOnMonitor(thread1, sync1));

    tracker.threadResumed(thread1);

    assertEquals(Collections.emptySet(), tracker.monitorExit(thread1, sync1));
    assertEquals(Collections.emptySet(), tracker.monitorExit(thread1, sync1));
    assertEquals(Collections.emptyMap(), tracker.getMonitors());
  }

  @Test
  public void interruptWakesUpThreadAfterMonitorIsReleasedByOtherThread() {
    assertEquals(Collections.emptySet(), tracker.monitorEnter(thread1, sync1));
    assertEquals(Collections.emptySet(), tracker.monitorEnter(thread1, sync1));
    assertEquals(Collections.emptySet(), tracker.wait(thread1, sync1));
    assertEquals(Collections.emptySet(), tracker.monitorEnter(thread2, sync1));

    //Before the interrupt, the thread should be waiting for a notify
    assertTrue(tracker.isWaitingForNotify(thread1, sync1));
    assertFalse(tracker.isBlockedOnMonitor(thread1, sync1));

    assertTrue(tracker.interrupt(thread1));

    //After the interrupt, the thread should be blocked on the monitor (waiting for a release)
    assertFalse(tracker.isWaitingForNotify(thread1, sync1));
    assertTrue(tracker.isBlockedOnMonitor(thread1, sync1));

    assertEquals(Collections.singleton(thread1), tracker.monitorExit(thread2, sync1));

    assertFalse(tracker.isBlockedOnMonitor(thread1, sync1));
    tracker.threadResumed(thread1);

    assertEquals(Collections.emptySet(), tracker.monitorExit(thread1, sync1));
    assertEquals(Collections.emptySet(), tracker.monitorExit(thread1, sync1));
    assertEquals(Collections.emptyMap(), tracker.getMonitors());
  }


}