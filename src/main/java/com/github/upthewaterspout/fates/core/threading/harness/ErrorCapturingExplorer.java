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

import java.util.Set;

import com.github.upthewaterspout.fates.core.states.StateExplorer;

/**
 * A state explorer that captures errors and saves them for the done method.
 *
 * There are some valid errors that an explorer can through due to problems with the user's test.
 * However, if those errors are thrown in the middle of instrumentation of things like wait and
 * notify, it can result in a hang.
 *
 * Instead, save the errors for the end of the test execution.
 */
public class ErrorCapturingExplorer implements StateExplorer {

  private final StateExplorer delegate;
  private RuntimeException exception;

  public ErrorCapturingExplorer(StateExplorer delegate) {
    this.delegate = delegate;
  }

  @Override
  public void done() {
    if (exception == null) {
      delegate.done();
    } else {
      System.err.println(this.getTrace());
      throw new IllegalStateException("Hit error during testing", exception);
    }

  }

  @Override
  public boolean isCompletelyTested() {
    if (exception == null) {
      return delegate.isCompletelyTested();
    } else {
      return true;
    }
  }

  @Override
  public long estimateIterations() {
    if (exception == null) {
      return delegate.estimateIterations();
    } else {
      return 0;
    }
  }

  @Override
  public String getTrace() {
    return delegate.getTrace();
  }

  @Override
  public <K> K decide(Object label, Set<K> options) {
    if (exception == null) {
      try {
        return delegate.decide(label, options);
      } catch (RuntimeException e) {
        this.exception = e;
      }
    }

    return options.stream().findFirst().orElse(null);
  }
}
