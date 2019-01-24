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

package com.github.upthewaterspout.fates.core.threading.event.confinement;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.github.upthewaterspout.fates.core.threading.event.ExecutionEventListener;
import org.junit.Before;
import org.junit.Test;

public class ThreadConfinementListenerTest {

  private ExecutionEventListener delegate;
  private ExecutionEventListener defaultAction;
  private ThreadConfinementListener listener;

  @Before
  public void setUp() throws Exception {
    delegate = mock(ExecutionEventListener.class);
    defaultAction = mock(ExecutionEventListener.class);
    listener = new ThreadConfinementListener(delegate);
  }

  @Test
  public void listenerPassesOnGetToSharedObject() {
    Object object = new Object();
    listener.beforeGetField(object, "any", "any", "any", 0);
    verify(delegate).beforeGetField(object, "any", "any", "any", 0);
  }

  @Test
  public void listenerElidesGetToThreadConfinedObject() {
    Object object = new Object();
    listener.afterNew(object);
    listener.beforeGetField(object, "any", "any", "any", 0);
    verify(delegate).afterNew(object);
    verifyNoMoreInteractions(delegate);
  }

  @Test
  public void listenerPassesOnSetToSharedObject() {
    Object object = new Object();
    listener.beforeSetField(object, "value", "any", "any", "any", 0);
    verify(delegate).beforeSetField(object, "value", "any", "any", "any", 0);
  }

  @Test
  public void listenerElidesSetToThreadConfinedObject() {
    Object object = new Object();
    listener.afterNew(object);
    listener.beforeSetField(object, "value", "any", "any", "any", 0);
    verify(delegate).afterNew(object);
    verifyNoMoreInteractions(delegate);
  }

  @Test
  public void objectBecomesSharedAfterSetOnSharedObject() {
    Object object = new Object();
    Object sharedObject = new Object();
    listener.afterNew(object);

    //Publish the object by setting it on a shared object
    listener.beforeSetField(sharedObject, object, "any", "any", "any", 0);

    //After the object is published, events should be passed on
    listener.beforeGetField(object, "any", "any", "any", 0);
    verify(delegate).beforeGetField(object, "any", "any", "any", 0);
  }

  @Test
  public void nestedConfinedObjectIsNotShared() {
    Object object = new Object();
    Object ownerObject = new Object();
    listener.afterNew(ownerObject);
    listener.afterNew(object);

    //Nest the thread confined object in another thread confined object
    listener.beforeSetField(ownerObject, object, "any", "any", "any", 0);

    listener.beforeGetField(object, "any", "any", "any", 0);

    verify(delegate).afterNew(object);
    verify(delegate).afterNew(ownerObject);
    //Operations on the nested object should not be passed along
    verifyNoMoreInteractions(delegate);
  }

  /**
   * Test that when an object becomes shared, everything reachable from that
   * object becomes shared
   */
  @Test
  public void nestedConfinedObjectIsPublishedWhenOwnerIsPublished() {
    Object object = new Object();
    ObjectWithReference ownerObject = new ObjectWithReference();
    Object sharedObject = new Object();

    listener.afterNew(ownerObject);
    listener.afterNew(object);

    //Nest the thread confined object in another thread confined object
    listener.beforeSetField(ownerObject, object, "any", "any", "any", 0);
    ownerObject.reference = object;

    //Make the ownerObject shared, which should also make the nested object shared
    listener.beforeSetField(sharedObject, ownerObject, "any", "any", "any", 0);


    listener.beforeGetField(object, "any", "any", "any", 0);

    //Operations on the nested object should now be passed along
    verify(delegate).beforeGetField(object, "any", "any", "any", 0);
  }

  private static class ObjectWithReference {
    Object reference;
  }


}