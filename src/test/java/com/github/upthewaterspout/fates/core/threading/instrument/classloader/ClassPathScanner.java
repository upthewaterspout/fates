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

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;


public class ClassPathScanner {

  public InputStream getResource(String name) throws IOException {
    String classpath = System.getProperty("java.class.path");
    String[] locations = classpath.split(File.pathSeparator);
    for(String location: locations) {
      if(location.endsWith(".jar")) {
        JarFile jar = new JarFile(location);
        ZipEntry zipEntry = jar.getEntry(name);
        if(zipEntry != null) {
          return new JarClosingInputStream(jar, jar.getInputStream(zipEntry));
        } else {
          jar.close();
        }
      }
    }

    return null;
  }

  private static class JarClosingInputStream extends FilterInputStream {

    private final JarFile jar;

    protected JarClosingInputStream(JarFile jar, InputStream in) {
      super(in);
      this.jar = jar;
    }

    @Override
    public void close() throws IOException {
      super.close();
      jar.close();;
    }
  }
}
