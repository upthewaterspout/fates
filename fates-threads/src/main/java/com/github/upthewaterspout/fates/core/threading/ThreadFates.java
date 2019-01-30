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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import com.github.upthewaterspout.fates.core.states.Fates;
import com.github.upthewaterspout.fates.core.states.StateExplorer;
import com.github.upthewaterspout.fates.core.threading.harness.ErrorCapturingExplorer;
import com.github.upthewaterspout.fates.core.threading.harness.Harness;

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
 *     ThreadFates.run(() -> {
 *       //Do some multhreaded test
 *       //Make assertions about the outcome
 *     });
 *   }
 * </pre>
 *
 */
public class ThreadFates {
  private List<Class<?>> atomicClasses = new ArrayList<>();
  public Fates fates = new Fates();

  /**
   * Enable execution traces. When this is turned on, a trace of all thread scheduling points from
   * the first pass through the test will be printed to standard out. This is useful for debugging
   * how many decision points exist within a test.
   * @param trace true to enable tracing
   * @return this
   */
  public ThreadFates setTrace(boolean trace) {
    fates.setTrace(trace);
    return this;
  }

  /**
   * Add a list of classes that should be treated as atomic with respect to the scheduler.
   *
   * Any code exercised within one a call to one of these classes will be executed atomically
   * - no scheduling will happen.
   *
   * @param atomicClasses A list of classes that should execute atomically during the test
   * @return this
   */
  public ThreadFates addAtomicClasses(Class<?> ... atomicClasses) {
    this.atomicClasses.addAll(Arrays.asList(atomicClasses));
    return this;
  }

  public ThreadFates setExplorer(Supplier<StateExplorer> explorer) {
    fates.setExplorer(() -> new ErrorCapturingExplorer(explorer.get()));
    return this;
  }

  /**
   * Run a multithreaded test in FATES. The test will be run many times, with all possible
   * thread execution orders.
   *
   * @param runnable The test to run
   * @throws Exception if the test fails.
   */
  public void run(MultiThreadedTest runnable) throws Exception {
    Harness.runTest(atomicClasses, fates, runnable);

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
