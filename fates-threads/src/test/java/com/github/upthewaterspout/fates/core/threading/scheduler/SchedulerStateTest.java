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

import com.github.upthewaterspout.fates.core.states.Decider;
import com.github.upthewaterspout.fates.core.states.explorers.depthfirst.DepthFirstExplorer;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;


public class SchedulerStateTest {

  @Test
  public void initiallyAddedThreadIsRunning() {
    Decider decider = mock(Decider.class);
    SchedulerState state = new SchedulerState(decider);

    Thread thread1 = new Thread();

    state.newThread(thread1, null);
    assertTrue(state.running(thread1));
  }


  @Test
  public void choosingWithOneThreadReturnsTheSameThread() {
    DepthFirstExplorer decider = new DepthFirstExplorer();
    SchedulerState state = new SchedulerState(decider);

    Thread thread1 = new Thread();

    state.newThread(thread1, null);
    assertEquals(thread1, state.chooseNextThread(thread1));
    assertFalse(decider.isCompletelyTested());
    assertTrue(state.running(thread1));
  }

  @Test
  public void choosingWithTwoThreadsRunningLeavesOneRunning() {
    Decider decider = new DepthFirstExplorer();
    SchedulerState state = new SchedulerState(decider);

    Thread thread1 = new Thread();
    Thread thread2 = new Thread();

    state.newThread(thread1, null);
    state.newThread(thread2, thread1);
    assertNull(state.chooseNextThread(thread1));
    assertTrue(state.running(thread2));
    assertFalse(state.running(thread1));
  }

  @Test
  public void choosingWithTwoThreadsParkedLeavesOneRunning() {
    Decider decider = new DepthFirstExplorer();
    SchedulerState state = new SchedulerState(decider);

    Thread thread1 = new Thread();
    Thread thread2 = new Thread();

    state.newThread(thread1, null);
    state.newThread(thread2, thread1);
    assertNull(state.chooseNextThread(thread1));
    Thread chosen = state.chooseNextThread(thread2);
    assertTrue(state.running(chosen));
    assertTrue(chosen == thread1 || chosen == thread2);
    Thread parked = chosen == thread1 ? thread2 : thread1;
    assertFalse(state.running(parked));
  }

  @Test
  public void unstartedThreadIsNotTerminated() {
    Decider decider = mock(Decider.class);
    SchedulerState state = new SchedulerState(decider);

    Thread thread1 = new Thread();

    state.newThread(thread1, null);
    assertTrue(state.running(thread1));
  }

  @Test
  public void terminatingAThreadWithOneRunningUnblocksTheOther(){
    Decider decider = new DepthFirstExplorer();
    SchedulerState state = new SchedulerState(decider);

    Thread thread1 = new Thread();
    Thread thread2 = new Thread();

    state.newThread(thread1, null);
    state.newThread(thread2, thread1);
    assertEquals(null, state.chooseNextThread(thread1));
    assertEquals(thread1, state.threadTerminated(thread2));
    assertTrue(state.running(thread1));
  }

  @Test
  public void eventsForUntrackedThreadsDoNotCorruptState() {
    Decider decider = mock(Decider.class);
    SchedulerState state = new SchedulerState(decider);

    Thread thread1 = new Thread();

    Assertions.assertThatThrownBy(() -> state.unpark(thread1)).isInstanceOf(IllegalStateException.class);
  }

  @Test
  public void interruptingAThreadMarksThreadAsInterrupted() {
    Decider decider = mock(Decider.class);
    SchedulerState state = new SchedulerState(decider);
    Thread thread1 = new Thread();
    state.newThread(thread1, null);

    assertFalse(state.isInterrupted(thread1, false));
    state.interrupt(thread1);
    assertTrue(state.isInterrupted(thread1, false));
  }

  @Test
  public void clearingTheInterruptStatusClearsTheInterrupt() {
    Decider decider = mock(Decider.class);
    SchedulerState state = new SchedulerState(decider);
    Thread thread1 = new Thread();
    state.newThread(thread1, null);

    state.interrupt(thread1);
    assertTrue(state.isInterrupted(thread1, true));
    assertFalse(state.isInterrupted(thread1, true));
  }

  @Test
  public void interruptingAParkedThreadUnParksTheThread() {
    Decider decider = mock(Decider.class);
    SchedulerState state = new SchedulerState(decider);
    Thread thread1 = new Thread();
    state.newThread(thread1, null);

    Thread thread2 = new Thread();
    state.newThread(thread2, thread1);
    state.park(thread1);

    assertTrue(state.isBlocked(thread1));
    assertFalse(state.running(thread1));
    assertFalse(state.isUnscheduled(thread1));

    state.interrupt(thread1);
    assertFalse(state.isBlocked(thread1));
    assertTrue(state.isUnscheduled(thread1));
    assertFalse(state.running(thread1));
  }

}