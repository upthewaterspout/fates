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
/**
 * This project contains two main components:
 *
 *  * {@link com.github.upthewaterspout.fates.core.states.StateExplorationHarness} - A general
 *  purpose testing harness for running tests many times, exploring the space of possible decisions
 *  a test can make.
 *  * {@link com.github.upthewaterspout.fates.core.threading.Fates} - A harness
 *  for running a multithreaded test many times, exploring all possible thread orderings of the test
 *  to eliminate race conditions.
 *
 *  Please see the javadocs for each harness for how to use them.
 */
package com.github.upthewaterspout.fates.core;