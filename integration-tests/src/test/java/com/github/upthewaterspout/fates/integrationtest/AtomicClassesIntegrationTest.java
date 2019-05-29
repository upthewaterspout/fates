package com.github.upthewaterspout.fates.integrationtest;

import static org.junit.Assert.assertEquals;

import com.github.upthewaterspout.fates.core.threading.ThreadFates;
import com.github.upthewaterspout.fates.executor.ParallelExecutor;
import org.junit.Test;

/**
 * Integration tests of {@link ThreadFates#addAtomicClasses(Class[])}
 */
public class AtomicClassesIntegrationTest {

  private static final int COUNT = 100;

  /**
   * Add an atomic class that touches many fields, and make sure
   * that it does not trigger a large number of scheduling combinations, since
   * the methods are atomic
   */
  @Test(timeout = Constants.TIMEOUT)
  public void atomicClassShouldNotTriggerScheduling() throws Throwable {

    new ThreadFates()
        .addAtomicClasses(AtomicClass.class)

        .run(() -> {
          AtomicClass instance = new AtomicClass();
          new ParallelExecutor<>()
              .inParallel("thread1", instance::touchFieldManyTimes)
              .inParallel("thread2", instance::touchFieldManyTimes)
              .run();
          assertEquals(2*COUNT, instance.field);
        });
  }

  public static class AtomicClass {
    private int field = 0;

    public Object touchFieldManyTimes() {
      for(int i =0; i < COUNT; i++) {
        synchronized (this) {
          field++;
        }
      }
      return null;
    }
  }
}
