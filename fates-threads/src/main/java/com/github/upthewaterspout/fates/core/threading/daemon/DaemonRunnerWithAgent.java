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

package com.github.upthewaterspout.fates.core.threading.daemon;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventSingleton;

/**
 * Utility for running a Callable in a separate JVM that has the fates java agent installed.
 *
 * The first call to {@link #execute(SerializableCallable)} will launch a separate JVM. Future
 * calls will reuse the same JVM. Concurrent {@link #execute(SerializableCallable)} calls are
 * not currently supported, they will be run serially.
 */
public class DaemonRunnerWithAgent {
  private static volatile DaemonRunner daemonRunner;

  public static <V> V execute(SerializableCallable<V> callable) throws Throwable {
    DaemonRunner runner = launchRunner();

    return runner.execute(callable);
  }

  private static synchronized DaemonRunner launchRunner()
      throws InterruptedException, ExecutionException, IOException, TimeoutException {
    if(daemonRunner == null) {
      daemonRunner = new DaemonRunner("-javaagent:" + findInstrumentationJar());
    }

    return daemonRunner;
  }

  private static String findInstrumentationJar() {
    Class<ExecutionEventSingleton> instrumentationClass = ExecutionEventSingleton.class;
    return instrumentationClass.getProtectionDomain().getCodeSource().getLocation().getFile();
  }
}
