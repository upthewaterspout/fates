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

package com.github.upthewaterspout.fates.core.threading.harness;

import static org.mockito.Mockito.*;

import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventListener;
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
      listener.beforeGetField("class", "method", 0);
    });
    listener.beforeThreadStart(thread);
    thread.start();
    listener.afterThreadStart(thread);
    thread.join();
    verify(delegate).beforeGetField(any(), any(), anyInt());
  }

  @Test
  public void notEnabledForUntrackedThread() throws InterruptedException {
    Thread thread = new Thread(() -> {
      listener.beforeGetField("class", "method", 0);
    });
    thread.start();
    thread.join();
    verify(delegate, times(0)).beforeGetField(any(), any(), anyInt());
  }

  @Test
  public void shouldDisableDuringThreadStart() {
    Thread thread = new Thread();
    listener.beforeThreadStart(thread);
    listener.beforeSetField("class", "method", 0);
    verify(delegate, times(0)).beforeGetField(any(), any(), anyInt());
    listener.afterThreadStart(thread);
    listener.beforeGetField("class", "method", 1);
    verify(delegate, times(1)).beforeGetField(any(), any(), anyInt());
  }

}