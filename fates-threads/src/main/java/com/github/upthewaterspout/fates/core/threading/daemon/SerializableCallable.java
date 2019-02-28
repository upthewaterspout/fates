package com.github.upthewaterspout.fates.core.threading.daemon;

import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 * Interface that combines serializable with callable. Created to make easier to use
 * lamba expressions with {@link DaemonRunner#execute(SerializableCallable)}
 */
public interface SerializableCallable<V> extends Callable<V>, Serializable {
}
