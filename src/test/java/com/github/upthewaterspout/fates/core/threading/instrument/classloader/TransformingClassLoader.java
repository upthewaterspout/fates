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

package com.github.upthewaterspout.fates.core.threading.instrument.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

/**
 * A non delegating class loader that transforms
 * the bytes of the class using ASM. 
 *
 */
public class TransformingClassLoader extends ClassLoader {

  private final ClassFileTransformer transformer;
  private final Pattern packages;
  private final Class<?> classBeingRedefined;

  public TransformingClassLoader(ClassFileTransformer transformer, String packagesRegex) {
    this(transformer, packagesRegex, null);
  }

  public TransformingClassLoader(ClassFileTransformer transformer, String packagesRegex, Class<?> classBeingRedefined) {
    super(Thread.currentThread().getContextClassLoader());
    this.transformer = transformer;
    this.packages = Pattern.compile(packagesRegex);
    this.classBeingRedefined = classBeingRedefined;
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    if(!packages.matcher(name).matches()) {
      return super.loadClass(name, resolve);
    }
    return findClass(name);
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    String className = name.replace(".", "/") + ".class";
    //This doesn't work for JDK classes because the JDK won't let us find
    //the JDK class and even if we work around that, the JDK won't let us
    //define the JDK class in our classloader.
    //Technically, we could probably transform all of the code to point to tranformed version of
    //JDK classes, but that would make debugging painful.
    try {
      InputStream is = getParent().getResourceAsStream(className);
      if (is == null) {
        throw new IllegalStateException("Could not find class with name " + name);
      }
      byte[] classBytes;
      classBytes = IOUtils.toByteArray(is);
      is.close();
      byte[] transformedBytes = transformer.transform(this, name, classBeingRedefined, null, classBytes);

      return defineClass(name, transformedBytes, 0, transformedBytes.length);
    } catch (IOException | IllegalClassFormatException e) {
      throw new IllegalStateException(e);
    }
  }

}
