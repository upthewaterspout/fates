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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * RMI object that lives in the lauching JVM. The {@link FatesDaemon} JVM will call
 * back to this object to register itself, and to check to make sure the launching JVM is still running
 */
class Controller extends UnicastRemoteObject implements ControllerRemote {
  CompletableFuture<DaemonRemote> daemon = new CompletableFuture<>();

  protected Controller() throws RemoteException {
  }

  @Override
  public void setDaemon(DaemonRemote daemon) throws RemoteException {
    this.daemon.complete(daemon);

  }

  @Override
  public void ping() throws RemoteException {
    //Do nothing

  }

  public DaemonRemote waitForDaemon(long time, TimeUnit unit)
      throws ExecutionException, InterruptedException, TimeoutException {
    return this.daemon.get(time, unit);
  }
}
