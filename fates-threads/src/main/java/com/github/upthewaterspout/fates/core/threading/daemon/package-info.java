/**
 * This package contains tools for running ThreadFates in a separate JVM, so that it can be launched from within
 * unit tests that do not have the {@link com.github.upthewaterspout.fates.core.threading.instrument.agent.FatesAgent}
 * loaded.
 *
 * See {@link com.github.upthewaterspout.fates.core.threading.daemon.DaemonRunnerWithAgent} for the main
 * entry point to this package.
 */
package com.github.upthewaterspout.fates.core.threading.daemon;