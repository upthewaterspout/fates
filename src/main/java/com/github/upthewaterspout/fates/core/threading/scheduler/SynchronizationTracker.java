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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Convert java synchronization, wait, and notify into scheduler events
 * park and unpark.
 *
 */
public class SynchronizationTracker {
  private final Map<Object, MonitorInfo> monitors = new IdentityHashMap<>();
  private final Map<Thread, Object> waitingToResume = new HashMap<>();

  /**
   * Indicate that a thread is trying to get a monitor.
   * @param sync
   * @return a list of threads that are blocked now that this
   * monitor is held
   */
  public Collection<Thread> monitorEnter(Thread currentThread, final Object sync) {
    final MonitorInfo currentHolder = monitors.get(sync);
    if(currentHolder == null) {
      monitors.put(sync, new MonitorInfo(currentThread));
      return Collections.emptySet();
    }

    if(currentHolder.owner == null) {
      currentHolder.setOwner(currentThread);
      waitingToResume.remove(currentThread);
      currentHolder.waitingForMonitor.remove(currentThread);
      return currentHolder.waitingForMonitor.keySet();
    }

    if(currentHolder.owner.equals(currentThread)) {
      currentHolder.depth++;
      return currentHolder.waitingForMonitor.keySet();
    }

    currentHolder.waitingForMonitor.put(currentThread, 1);
    return Collections.singleton(currentThread);
  }

  /**
   * Indicates we're exiting a synchronization block.
   * Returns the list of members that can now be unblocked
   */
  public Collection<Thread> monitorExit(Thread currentThread, final Object sync) {
    final MonitorInfo monitorInfo = getMonitorInfo(currentThread, sync);

    if(--monitorInfo.depth > 0) {
      return Collections.emptySet();
    }

    monitorInfo.drainPendingNotifies();

    if(monitorInfo.waitingForNotify.isEmpty() && monitorInfo.waitingForMonitor.isEmpty()) {
      monitors.remove(sync);
    }

    monitorInfo.owner = null;

    monitorInfo.waitingForMonitor.keySet().forEach(thread -> waitingToResume.put(thread, sync));
    return monitorInfo.waitingForMonitor.keySet();
  }

  public Collection<Thread> wait(Thread currentThread, final Object sync) {
    final MonitorInfo monitorInfo = getMonitorInfo(currentThread, sync);

    monitorInfo.drainPendingNotifies();
    monitorInfo.waitingForNotify.put(currentThread, monitorInfo.depth);
    monitorInfo.waitingForMonitor.keySet().forEach(thread -> waitingToResume.put(thread, sync));
    monitorInfo.depth = 0;
    monitorInfo.owner = null;
    return monitorInfo.waitingForMonitor.keySet();
  }

  private MonitorInfo getMonitorInfo(final Thread currentThread, final Object sync) {
    final MonitorInfo monitorInfo = monitors.get(sync);
    if(monitorInfo == null || monitorInfo.owner == null || !monitorInfo.owner.equals(currentThread)) {
      throw new IllegalMonitorStateException("Monitor not held " + sync);
    }
    return monitorInfo;
  }

  public void notify(Thread currentThread, final Object sync) {
    final MonitorInfo monitorInfo = getMonitorInfo(currentThread, sync);

    monitorInfo.pendingNotifies++;
  }

  public void notifyAll(Thread currentThread, final Object sync) {
    final MonitorInfo monitorInfo = getMonitorInfo(currentThread, sync);

    monitorInfo.pendingNotifies = Integer.MAX_VALUE / 2;
  }

  /**
   * For Testing only, return the set of monitors.
   */
  Map<Object, MonitorInfo> getMonitors() {
    return monitors;
  }

  /**
   * Indicate that a thread is resuming execution
   * @return a set of threads which are now blocked because the resumed
   * thread will acquire the monitor
   */
  public Collection<Thread> threadResumed(final Thread scheduledThread) {
    Object monitor = waitingToResume.remove(scheduledThread);
    if(monitor != null) {
      return monitorEnter(scheduledThread, monitor);
    } else {
      return Collections.emptySet();
    }
  }

  private static class MonitorInfo {
    private Thread owner;
    private int depth = 1;
    private final Map<Thread, Integer> waitingForMonitor = new LinkedHashMap<Thread, Integer>(2);
    private final Map<Thread, Integer> waitingForNotify = new LinkedHashMap<Thread, Integer>(2);
    public int pendingNotifies;

    public MonitorInfo(final Thread thread) {
      this.owner = thread;
    }

    private void drainPendingNotifies() {
      Iterator<Map.Entry<Thread, Integer>> iterator = waitingForNotify.entrySet().iterator();
      while(pendingNotifies > 0 && iterator.hasNext()) {
        pendingNotifies--;
        final Entry<Thread, Integer> entry = iterator.next();
        waitingForMonitor.put(entry.getKey(), entry.getValue());
        iterator.remove();;
      }

      pendingNotifies = 0;
    }

    private void setOwner(final Thread currentThread) {
      owner = currentThread;
      if(waitingForMonitor.containsKey(currentThread)) {
        depth = waitingForMonitor.remove(currentThread);
      } else {
        depth = 1;
      }
    }
  }
}
