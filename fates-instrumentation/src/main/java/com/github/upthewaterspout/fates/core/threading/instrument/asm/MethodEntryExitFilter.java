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

package com.github.upthewaterspout.fates.core.threading.instrument.asm;

import java.util.function.BiPredicate;

/**
 * A filter for methods that should have entry and exit instrumented.
 *
 * Instrumenting every single method call is expensive, so we only instrument
 * methods where required.
 */
public interface MethodEntryExitFilter {

  /**
   * Return true if the method is instrumented
   * @param className The binary name of the class, as returned by {@link Class#getName()}
   * @param methodName The name of the method being invoked
   * @return true if the method should be instrumented
   */
  boolean test(String className, String methodName);
}
