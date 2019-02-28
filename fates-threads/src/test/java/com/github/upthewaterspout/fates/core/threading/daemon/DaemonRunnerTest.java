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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DaemonRunnerTest {

  @Test
  public void executeShouldUseJVM() throws Throwable {
    DaemonRunner runner = new DaemonRunner();

    assertEquals("test", runner.execute(() -> "test"));
    runner.destroy();
  }

  @Test
  public void multipleExecuteCallsShouldReuseJVM() throws Throwable {
    DaemonRunner runner = new DaemonRunner();

    String propertyName = "DaemonRunnerTest";
    assertEquals(null, runner.execute(() -> System.setProperty(propertyName, "test")));
    assertEquals("test", runner.execute(() -> System.getProperty(propertyName)));

    //Make sure execution happened in a separate JVM
    assertEquals(null, System.getProperty(propertyName));

    runner.destroy();
  }
}