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

package com.github.upthewaterspout.fates.core.threading;

import com.github.upthewaterspout.fates.core.states.Decider;
import com.github.upthewaterspout.fates.core.states.RepeatedTest;
import com.github.upthewaterspout.fates.core.states.StateExplorationHarness;
import com.github.upthewaterspout.fates.core.states.StateExplorer;
import com.github.upthewaterspout.fates.core.states.depthfirst.DepthFirstExplorer;
import com.github.upthewaterspout.fates.core.threading.confinement.ThreadConfinementListener;
import com.github.upthewaterspout.fates.core.threading.harness.AtomicClassLoadingDecorator;
import com.github.upthewaterspout.fates.core.threading.harness.ErrorCapturingExplorer;
import com.github.upthewaterspout.fates.core.threading.harness.ThreadLocalEventListener;
import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventListener;
import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventSingleton;
import com.github.upthewaterspout.fates.core.threading.scheduler.ThreadSchedulingListener;

/**
 * A harness for running a callable that launches multiple threads with all possible
 * thread orderings.
 * <p>
 * This harness will take the callable and run it many times, controlling the ordering of threads
 * each time, until it has explored all possible orderings.
 * <p>
 * The harness requires that a -javaagent parameter is passed to the JVM that it is executing in.
 * For example
 * <pre>
 *   java -javaagent:/path/to/fates-all.jar ...
 * </pre>
 * <p>
 * The agent will extensively modify the bytecode being used. Although the modifications should have
 * no effect other than performance when a test is not running in this harness, it's still good
 * practice to only include it for tests that are using this harness.
 * <p>
 * Example:
 *
 * <pre>
 *   {@code
 *     Fates.run(() -> {
 *       //Do some multhreaded test
 *       //Make assertions about the outcome
 *     });
 *   }
 * </pre>
 *
 */
public class Fates {

  /**
   * Run a multithreaded test in FATES. The test will be run many times, with all possible
   * thread execution orders.
   *
   * @param runnable The test to run
   * @throws Exception if the test fails.
   */
  public static void run(MultiThreadedTest runnable) throws Exception {
    StateExplorer explorer = new ErrorCapturingExplorer(new DepthFirstExplorer());

    //Do one run without the harness to try to get classloading, etc out of the way
    runnable.run();

    //Use the state exploration harness to explore the possible thread orderings
    StateExplorationHarness.explore(explorer, instrumentTest(runnable));

  }

  /**
   * Convert a {@link MultiThreadedTest}, which uses threads, into a {@link RepeatedTest},
   * which which has a bunch of decision points, by enabling instrumentation and using
   * the {@link Decider} to pick which thread ordering to use
   */
  private static RepeatedTest instrumentTest(MultiThreadedTest runnable) {
    return decider -> {

      ExecutionEventListener listener = createExecutionEventPipeline(decider);

      ExecutionEventSingleton.setListener(listener);
      try {
        runnable.run();
      } finally {
        ExecutionEventSingleton.setListener(null);
      }
    };
  }

  /**
   * Create the pipeline of listeners for processing execution events during a single run
   * of the test. This pipeline will control the order of threads in the test
   *
   * @param decider The decider used to choose which thread to allow to proceed for this test
   */
  private static ExecutionEventListener createExecutionEventPipeline(Decider decider) {

    //At the end of the pipeline is the actual thread scheduler
    ThreadSchedulingListener scheduler = new ThreadSchedulingListener(decider);
    scheduler.begin();

    //In front of that is a listener that can skip events if we are doing class loading
    ExecutionEventListener listener
        = new AtomicClassLoadingDecorator( scheduler);

    //In front of that is a listener that detects if objects are only used by a single thread
    listener = new ThreadConfinementListener(listener);

    //In front of that is a listener which restricts instrumentation to threads started by
    //this test
    listener = new ThreadLocalEventListener(listener);

    return listener;
  }

  /**
   * A multithreaded test to run in FATES.
   * <p>
   *
   * This is a separate interface and not {@link Runnable} just so that the test can throw
   * an exception without needed special handling.
   */
  public interface MultiThreadedTest {
    void run() throws Exception;
  }

}
