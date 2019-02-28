package com.github.upthewaterspout.fates.core.threading.daemon;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Main method of the daemon JVM. This method will create an RMI object to receive requests,
 * and will keep running until it can no longer ping the launching JVM through the {@link Controller}
 */
class FatesDaemon {

  public static final int PING_TIME = 1000;

  public static void main(String[] args)
      throws IOException, ClassNotFoundException, InterruptedException {
    ObjectInputStream in = new ObjectInputStream(System.in);
    ControllerRemote  controller = (ControllerRemote) in.readObject();

    DaemonRemote daemon = new Daemon();
    controller.setDaemon((DaemonRemote) UnicastRemoteObject.toStub(daemon));

    try {
      while (true) {
        Thread.sleep(PING_TIME);
        controller.ping();
      }
    } catch (RemoteException e) {
      //controller has shut down
    } finally {
      System.exit(0);
    }
  }
}
