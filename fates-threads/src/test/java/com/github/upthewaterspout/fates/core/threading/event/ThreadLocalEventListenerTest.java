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

package com.github.upthewaterspout.fates.core.threading.event;

import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;

import com.github.upthewaterspout.fates.core.threading.event.ExecutionEventListener;
import com.github.upthewaterspout.fates.core.threading.event.ThreadLocalEventListener;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

public class ThreadLocalEventListenerTest {


  private ExecutionEventListener delegate;
  private ThreadLocalEventListener listener;

  @Before
  public void setup() {
    delegate = mock(ExecutionEventListener.class);
    listener = new ThreadLocalEventListener(delegate);
  }

  @Test
  public void enabledForTrackedThread() throws InterruptedException {
    Thread thread = new Thread(() -> {
      listener.beforeGetField("any", "any", "class", "method", 0);
    });
    listener.beforeThreadStart(thread);
    thread.start();
    listener.afterThreadStart(thread);
    thread.join();
    verify(delegate).beforeGetField(any(), any(), any(), any(), anyInt());
  }

  @Test
  public void notEnabledForUntrackedThread() throws InterruptedException {
    Thread thread = new Thread(() -> {
      listener.beforeGetField("owner", "any", "class", "method", 0);
    });
    thread.start();
    thread.join();
    verify(delegate, times(0)).beforeGetField(any(), any(), any(), any(), anyInt());
  }

  @Test
  public void shouldDisableDuringThreadStart() {
    Thread thread = new Thread();
    listener.beforeThreadStart(thread);
    listener.beforeSetField(null, null, "any", "class", "method", 0);
    verify(delegate, times(0)).beforeGetField(any(), any(), any(), any(), anyInt());
    listener.afterThreadStart(thread);
    listener.beforeGetField("owner", "any", "class", "method", 1);
    verify(delegate, times(1)).beforeGetField(any(), any(), any(), any(), anyInt());
  }

  @Test
  public void postValidationWaitsForDanglingThreads() throws InterruptedException {
    Thread thread = new Thread();
    listener.beforeThreadStart(thread);
    listener.afterThreadStart(thread);

    listener.postValidation();
    verify(delegate).replaceJoin(any(), eq(thread), eq(0L), eq(0));
  }

  @Test
  public void postValidationSucceedsWithNoDanglingThreads() {
    Thread thread = new Thread();
    listener.beforeThreadStart(thread);
    listener.afterThreadStart(thread);
    listener.beforeThreadExit(thread);
    listener.postValidation();
  }

}