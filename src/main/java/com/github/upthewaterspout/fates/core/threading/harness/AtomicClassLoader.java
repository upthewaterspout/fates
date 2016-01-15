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

import com.github.upthewaterspout.fates.core.threading.Fates;

/**
 * A class loader that used the given {@link AtomicControl} during classloading operations
 * to mark the class loading as atomic.
 *
 * This is used by the {@link Fates} to not bother testing for race conditions during
 * classloading.
 */
public class AtomicClassLoader extends ClassLoader {
  AtomicControl atomicControl;

  public AtomicClassLoader(final ClassLoader parent, final AtomicControl atomicControl) {
    super(parent);
    this.atomicControl = atomicControl;
  }

  @Override protected Class<?> loadClass(final String name, final boolean resolve)
      throws ClassNotFoundException {
    atomicControl.beginAtomic();
    try {
      return super.loadClass(name, resolve);
    } finally {
      atomicControl.endAtomic();
    }
  }

  @Override
  public Class<?> loadClass(final String name) throws ClassNotFoundException {
    atomicControl.beginAtomic();
    try {
      return super.loadClass(name);
    } finally {
      atomicControl.endAtomic();
    }
  }
}
