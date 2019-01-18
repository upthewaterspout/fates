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

package com.github.upthewaterspout.fates.core.threading.instrument.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.regex.Pattern;

/**
 * A {@link ClassFileTransformer} that only transforms classes that match
 * specific patterns.
 */
public class FilterTransformer implements ClassFileTransformer {

  private final String[] excludedPackages;
  private ClassFileTransformer delegate;

  public FilterTransformer(ClassFileTransformer delegate, String ... excludedPackages) {
    this.excludedPackages = excludedPackages;
    this.delegate = delegate;
  }
  @Override
  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                          ProtectionDomain protectionDomain, byte[] classfileBuffer)
      throws IllegalClassFormatException {
    if(!isExcluded(className)) {
      return delegate.transform(loader,
          className,
          classBeingRedefined,
          protectionDomain,
          classfileBuffer);
    }
    return null;
  }

  public boolean isExcluded(String className) {
    for(String pakage : excludedPackages) {
      if(className.startsWith(pakage)) {
        return true;
      }
    }

    return false;
  }
}
