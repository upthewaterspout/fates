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

package com.github.upthewaterspout.fates.core.threading.instrument.monitor;

import sun.misc.Unsafe;

public class DefaultMonitorControl implements MonitorControl {

  @Override
  public void monitorEnter(Object sync) {
    Unsafe.getUnsafe().monitorEnter(sync);
  }

  @Override
  public void monitorExit(Object sync) {
    Unsafe.getUnsafe().monitorExit(sync);
  }
}
