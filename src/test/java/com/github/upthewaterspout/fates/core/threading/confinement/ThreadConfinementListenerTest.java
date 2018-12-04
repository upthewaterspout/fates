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

package com.github.upthewaterspout.fates.core.threading.confinement;

import static org.mockito.Mockito.mock;

import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventListener;
import org.junit.Test;

public class ThreadConfinementListenerTest {

  @Test
  public void listenerIgnoresCallsToThreadConfinedObject() {
    ExecutionEventListener delegate = mock(ExecutionEventListener.class);
    ExecutionEventListener defaultAction = mock(ExecutionEventListener.class);
    ThreadConfinementListener listener = new ThreadConfinementListener(delegate);

    listener.beforeGetField("owner", "any", "any", 0);
    listener.beforeSetField("owner", "value", "class", "method", 0);

    //TODO, ignore all of these calls
    //This gets a bit tricky, because we need the effects of these calls
    //To be consistent, even if we publish the sync object in the middle of the synchronized
    //block
//    listener.beforeSynchronization("someobject");
//    listener.afterSynchronization("someobject");
//    listener.replaceWait();
//    listener.replaceNotify();
//    listener.replaceNotifyAll();
  }

}