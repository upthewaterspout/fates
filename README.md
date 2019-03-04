[![Build Status](https://travis-ci.org/upthewaterspout/fates.svg?branch=master)](https://travis-ci.org/upthewaterspout/fates)

FATES stands for Find All Thread Execution Schedules. It is a framework for
testing multi-threaded Java applications. 

Fates contains two sub-modules

* **fates-explore** - A utility that runs a test with *decision points* repeatedly, until
all possible decisions are tested
* **fates-threads** - A tool for finding race conditions in multi-threaded java code. It instruments the bytecode of the
program under test and takes control of thread scheduling. It then runs the
test repeatedly until all possible scheduling orders are tested.

This framework is in very early stages of development, and will not
successfully run for anything but very trivial cases.

# How to use

## Multithreaded tests

In your test code, run your multithreaded test using the ThreadFates class. For example, using junit, here is a simple test of whether it is safe for two threads to call ++ on an integer concurrently (spoiler - it's not). Fates will run this test in all possible ways the two threads can be interleaved. Some of these orderings result in an assertion error, showing us that this code is not threadsafe!

```java
public class UnsynchronizedUpdateTest {

  @Test()
  public void incrementShouldBeThreadSafe() throws Throwable {
    new ThreadFates().run(() -> {
      UnsafeInteger integer = new UnsafeInteger();
      Thread thread1 = new Thread(integer::increment, "thread1");
      Thread thread2 = new Thread(integer::increment, "thread2");
      thread1.start();
      thread2.start();
      thread1.join();
      thread2.join();

      assertEquals(2, integer.getValue());
    });
  }

  private static class UnsafeInteger {
    int value = 0;

    public void increment() {
      value++;
    }

    public int getValue() {
      return value;
    }
  }

}
```

The two useful classes from a user perspective are:
* [ThreadFates](https://upthewaterspout.github.io/fates/javadoc/fates-threads/index.html?com/github/upthewaterspout/fates/core/threading/ThreadFates.html)
The main harness for running multithreaded tests
* [Fates](https://upthewaterspout.github.io/fates/javadoc/fates-explore/index.html?com/github/upthewaterspout/fates/core/states/Fates.html)
A harness for running any test that has decision points repeatedly until all
possible decisions are exercised

# How it works

This harness creates a scheduler which takes control of the order in which your
threads read or modify any field.  This agent works modifying the bytecode of
all classes in the test (including JDK classes) to instrument all field access.
Each field access is then used as a point where the scheduler can choose to let
that thread continue, or schedule a different thread.

Operations which might require a thread to block are also instrumented -
synchronization blocks, waits, etc. These operations are replaced by calls to
the scheduler.

The test is run repeatedly until all possible schedules are exercised.

## Interesting classes
 * The `fates-explore` module with the `Fates` class and the `states` package - this package contains 
 all of the logic to execute a test multiple times and explore all of the possible
 choices a test might make. This module does not do any bytecode instrumentation 
 and isn't tied to multi-threaded testing.
 
 * Within the `fates-threads` module:
   * `SharedStateSpaceScheduler` - this is the class that actually tries to
 control the order of the threads. It uses the `Decider` provided by the
 `StateExplorationHarness` to choose which thread to schedule at each point in
 time
   * `ExecutionEventSingleton` - this class has all of the events that the bytecode 
 instrumentation calls.
   * `AsmTransformer` - This class builds the pipeline of `ClassVisitors` that actually
 modify the user's bytecode


# Caveats

## Long running tests

Tests that access many fields lead to a large number of possible thread
orderings.  This framework currently does not do a good job of reducing the
choices to only interesting thread orderings - every field access is a decision
point. This means that tests may take a *very* long time to complete.

## Classloading
Currently the scheduler is using a classloader that disables instrumentation
during classloading. That means it may miss race conditions that occur during
classloading. 

## Blocked thread handling

LockSupport, synchronization, wait, notify, etc. are all handled by the
scheduler. But there could be other events that cause a thread to block, for
example a blocking IO operation that depends on another thread. These
operations will currently just cause the test to hang.

## Timed waits

Timed wait calls are currently no ops, because it's possible the thread in a timed
wait could pick up and continue without other threads running.
