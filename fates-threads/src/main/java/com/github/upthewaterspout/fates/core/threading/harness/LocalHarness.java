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

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.URLClassLoader;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.upthewaterspout.fates.core.states.Decider;
import com.github.upthewaterspout.fates.core.states.ExplorerSupplier;
import com.github.upthewaterspout.fates.core.states.Fates;
import com.github.upthewaterspout.fates.core.states.RepeatedTest;
import com.github.upthewaterspout.fates.core.threading.ThreadFates;
import com.github.upthewaterspout.fates.core.threading.event.AtomicClassLoadingDecorator;
import com.github.upthewaterspout.fates.core.threading.event.AtomicMethodListener;
import com.github.upthewaterspout.fates.core.threading.event.ExecutionEventListener;
import com.github.upthewaterspout.fates.core.threading.event.IgnoreFinalFieldsListener;
import com.github.upthewaterspout.fates.core.threading.event.ThreadLocalEventListener;
import com.github.upthewaterspout.fates.core.threading.event.confinement.ThreadConfinementListener;
import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventSingleton;
import com.github.upthewaterspout.fates.core.threading.scheduler.ThreadSchedulingListener;

public class LocalHarness implements Harness {
  private static final List<Class<?>> DEFAULT_PUBLIC_ATOMIC_CLASSES = Arrays.asList(
      SecurityManager.class, System.class, AccessControlContext.class, AccessController.class,
      Class.class, ClassLoader.class, URLClassLoader.class, Field.class,
      Integer.class, Long.class, Character.class, Short.class, Byte.class, Boolean.class, Float.class, Double.class, String.class,
      InetAddress.class, ThreadGroup.class);

  private static final List<String> DEFAULT_INTERNAL_ATOMIC_CLASSES = Arrays.asList("java.lang.invoke.MethodHandleNatives", "sun.instrument.InstrumentationImpl");

  public static final List<String> DEFAULT_ATOMIC_CLASS_NAMES = Stream.concat(DEFAULT_INTERNAL_ATOMIC_CLASSES.stream(), DEFAULT_PUBLIC_ATOMIC_CLASSES.stream().map(Class::getName)).collect(
      Collectors.toList());



  @Override
  public void runTest(List<Class<?>> atomicClasses, Fates fates,
                      ThreadFates.MultiThreadedTest runnable) throws Exception {

    for(int i =0; i < 20; i++) {
      runnable.run();
    }

    List<String> atomicClassNames = Stream.concat(DEFAULT_ATOMIC_CLASS_NAMES.stream(), atomicClasses.stream().map(Class::getName)).collect(
        Collectors.toList());
    //Use the state exploration harness to explore the possible thread orderings
    fates.explore(instrumentTest(atomicClassNames, runnable));
  }

  /**
   * Convert a {@link ThreadFates.MultiThreadedTest}, which uses threads, into a {@link RepeatedTest},
   * which which has a bunch of decision points, by enabling instrumentation and using
   * the {@link Decider} to pick which thread ordering to use
   */
  private RepeatedTest instrumentTest(List<String> atomicClasses,
                                             ThreadFates.MultiThreadedTest runnable) {
    return decider -> {

      ExecutionEventListener listener = createExecutionEventPipeline(atomicClasses, decider);

      ExecutionEventSingleton.setListener(listener);
      try {
        runnable.run();
        ExecutionEventSingleton.postValidation();
      } finally {
        ExecutionEventSingleton.setListener(null);
      }
    };
  }

  /**
   * Create the pipeline of listeners for processing execution events during a single run
   * of the test. This pipeline will control the order of threads in the test
   * @param decider The decider used to choose which thread to allow to proceed for this test
   * @param atomicClasses The classes to consider atomic. See {@link AtomicMethodListener}
   * @return the execution pipeline
   */
  public ExecutionEventListener createExecutionEventPipeline(List<String> atomicClasses,
                                                                    Decider decider) {

    //At the end of the pipeline is the actual thread scheduler
    ThreadSchedulingListener scheduler = new ThreadSchedulingListener(decider);
    scheduler.begin();

    ExecutionEventListener listener = scheduler;
    //In front of that is a listener that suppresses events on final fields
    listener = new IgnoreFinalFieldsListener(listener);

    //In front of that is a listener that suppresses events for calls with atomicClasses
    listener = new AtomicMethodListener(listener, atomicClasses);

    //In front of that is a listener that can skip events if we are doing class loading
    listener = new AtomicClassLoadingDecorator( listener);

    //In front of that is a listener that detects if objects are only used by a single thread
    listener = new ThreadConfinementListener(listener);

    //In front of that is a listener which restricts instrumentation to threads started by
    //this test
    listener = new ThreadLocalEventListener(listener);

    return listener;
  }
}
