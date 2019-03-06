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

package com.github.upthewaterspout.fates.core.threading.scheduler;

import java.util.Objects;

/**
 * Data object that stores a line number and the thread that visited it.
 */
class LineNumber {
  private final String currentThread;
  private final String className;
  private final String methodName;
  private final int lineNumber;

  public LineNumber(String currentThread, String className, String methodName,
                    int lineNumber) {
    this.currentThread = currentThread;
    this.className = className;
    this.methodName = methodName;
    this.lineNumber = lineNumber;
  }

  @Override
  public String toString() {
    return  className + "." + methodName + "(" + getShortClassName()
        + ".java:" + lineNumber + ")(" + currentThread + ")";
  }

  private String getShortClassName() {
    if (className.lastIndexOf(".") == -1) {
      return className;
    } else {
      return className.substring(className.lastIndexOf(".") + 1, className.length());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LineNumber that = (LineNumber) o;
    return lineNumber == that.lineNumber &&
        Objects.equals(className, that.className);
  }

  @Override
  public int hashCode() {

    return Objects.hash(className, lineNumber);
  }
}
