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

import static org.mockito.Mockito.spy;

import java.util.concurrent.Callable;

import com.github.upthewaterspout.fates.core.threading.event.ExecutionEventListener;
import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventSingleton;
import com.github.upthewaterspout.fates.core.threading.event.NoopExecutionEventListener;
import com.github.upthewaterspout.fates.core.threading.instrument.agent.FatesMethodEntryExitFilter;
import com.github.upthewaterspout.fates.core.threading.instrument.classloader.TransformingClassLoader;
import org.junit.After;
import org.junit.Before;

public class InstrumentationTest {
  protected final AsmTransformer transformer = new AsmTransformer(getMethodEntryExitFilter());
  protected ExecutionEventListener hook;
  private boolean wasAvailable;

  protected MethodEntryExitFilter getMethodEntryExitFilter() {
    return new FatesMethodEntryExitFilter();
  }

  @Before
  public void before() {
    hook = spy(new NoopExecutionEventListener());
    wasAvailable = ExecutionEventSingleton.setAvailable();
    ExecutionEventSingleton.setListener(hook);
  }

  @After
  public void after() {
    ExecutionEventSingleton.setListener(null);
    ExecutionEventSingleton.setAvailable(wasAvailable);
  }

  protected <T> Callable<T> transformAndCreate(String className)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    TransformingClassLoader loader = new TransformingClassLoader(transformer, "com.github.upthewaterspout.fates.core.threading.instrument.asm.instrumented.*");
    Class<?> clazz = loader.loadClass(className);
    return (Callable) clazz.newInstance();
  }
}
