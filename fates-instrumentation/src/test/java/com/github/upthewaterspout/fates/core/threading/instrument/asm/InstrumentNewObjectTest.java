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

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Callable;

import com.github.upthewaterspout.fates.core.threading.instrument.asm.instrumented.ClassWithNew;
import org.junit.Test;

public class InstrumentNewObjectTest extends InstrumentationTest {

    @Test
    public void newObjectHookIsInvoked() throws Exception {
      String className = ClassWithNew.class.getCanonicalName();
      Callable<Integer> object = transformAndCreate(className);
      Object value = object.call();
      verify(hook, times(1)).afterNew(eq(value));
    }

}