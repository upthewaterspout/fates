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

import java.util.List;

import com.github.upthewaterspout.fates.core.states.Fates;
import com.github.upthewaterspout.fates.core.threading.ThreadFates;
import com.github.upthewaterspout.fates.core.threading.daemon.DaemonRunnerWithAgent;

public class RemoteHarness implements  Harness {
  public void runTest(List<Class<?>> atomicClasses, Fates fates,
                             ThreadFates.MultiThreadedTest runnable) throws Throwable {
    DaemonRunnerWithAgent.execute(() -> {
      new LocalHarness().runTest(atomicClasses, fates, runnable);
      return null;
    });
  }
}
