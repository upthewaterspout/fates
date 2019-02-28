package com.github.upthewaterspout.fates.core.threading.daemon;

import java.io.*;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.upthewaterspout.fates.core.threading.instrument.ExecutionEventSingleton;

/**
 * Runs callables in a separate JVM. This runner launches the separate JVM on construction,
 * and reuses it for future calls to {@link #execute(SerializableCallable)}.
 *
 * For a {@link DaemonRunner} that has the fates java agent enabled, see {@link DaemonRunnerWithAgent}
 */
class DaemonRunner {

  private final DaemonRemote daemon;
  private final Controller controller;
  private final Process process;


  public DaemonRunner(String... extraJVMArgs)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    process = launchProcess(extraJVMArgs);
    ObjectOutputStream out = new ObjectOutputStream(process.getOutputStream());
    controller = new Controller();
    out.writeObject(UnicastRemoteObject.toStub(controller));
    out.flush();

    this.daemon = controller.waitForDaemon(5, TimeUnit.MINUTES);
  }

  public void destroy() throws IOException {
  {
    process.getOutputStream().close();
    process.destroyForcibly();
    UnicastRemoteObject.unexportObject(controller, true);
  }
}

  /**
   * Execute the callable in a separate JVM configured with the fates agent and return the result.
   * @param callable
   * @param <V>
   * @return
   */
  public <V> V execute(SerializableCallable<V> callable) throws Throwable {
    try {
      return daemon.execute(callable);
    } catch (RemoteException e) {
      throw e.getCause();
    }
  }

  private Process launchProcess(String[] extraJVMArgs) throws IOException {
    String java = System.getProperty("java.home") + File.separator
        + "bin" + File.separator + "java";
    String classpath = System.getProperty("java.class.path");

    ProcessBuilder builder = new ProcessBuilder();
    List<String> command = new ArrayList<>();
    command.addAll(Arrays.asList(java, "-cp", classpath));
    command.addAll(Arrays.asList(extraJVMArgs));
    command.add(FatesDaemon.class.getCanonicalName());
    builder.command(command);
    builder.redirectError(ProcessBuilder.Redirect.INHERIT);
    builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
    return builder.start();
  }

}
