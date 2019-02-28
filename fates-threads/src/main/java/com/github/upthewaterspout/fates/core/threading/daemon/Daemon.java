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

package com.github.upthewaterspout.fates.core.threading.daemon;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * RMI object that lives in the {@link FatesDaemon} JVM. The launching JVM can make calls into
 * the daemon JVM through this object's {@link #execute(SerializableCallable)} method.
 */
class Daemon extends UnicastRemoteObject implements DaemonRemote {

  public Daemon() throws RemoteException {
    super();
  }

  @Override
  public synchronized <V> V execute(SerializableCallable<V> callable) throws Exception {
    return callable.call();
  }
}
