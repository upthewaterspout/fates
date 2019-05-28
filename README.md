[![Build Status](https://travis-ci.org/upthewaterspout/fates.svg?branch=master)](https://travis-ci.org/upthewaterspout/fates)

FATES stands for Find All Thread Execution Schedules. It is a framework for
testing multi-threaded Java applications. 

Fates contains two sub-modules

* **fates-explore** - A utility that runs a test with *decision points* repeatedly, until
all possible decisions are tested
* **fates-threads** - A tool for finding race conditions in multi-threaded java code. It instruments the bytecode of the
program under test and takes control of thread scheduling. It then runs the
test repeatedly until all possible scheduling orders are tested. This framework is in very early stages of development.

# Installation

Fates is distributed through maven central. For multithreaded tests, just add the 
[fates-threads](https://search.maven.org/artifact/com.github.upthewaterspout.fates/fates-threads/) 
jar as a test dependency.

# How to use

## Multithreaded tests

In your test code, run your multithreaded test using the 
[ThreadFates](https://upthewaterspout.github.io/fates/javadoc/fates-threads/index.html?com/github/upthewaterspout/fates/core/threading/ThreadFates.html) 
class. For example, using junit, here is a simple test of whether it is safe for two threads to call ++ on an integer 
concurrently (spoiler - it's not). Fates will run this test in all possible ways the two threads 
can be interleaved. Some of these orderings result in an assertion error, showing us that this code 
is not threadsafe!

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

Fates also includes a simple ParallelExecutor class that simplifies launching parallel threads
and joing them in your test. It's designed to work well with the testing harness. The above test 
can be simplified using this parallel executor like so.

```java
  @Test()
  public void incrementShouldBeThreadSafe() throws Throwable {
    new ThreadFates().run(() -> {
      UnsafeInteger integer = new UnsafeInteger();
      new ParallelExecutor()
              .inParallel("updater1", updater::update)
              .inParallel("updater2", updater::update)
              .run();

      assertEquals(2, integer.getValue());
    });
  }

``` 


## Repeating tests with decision points in them
Fates is not limited to testing multithreaded code. The ThreadFates harness is built on top of the more general purpose
[Fates](https://upthewaterspout.github.io/fates/javadoc/fates-explore/index.html?com/github/upthewaterspout/fates/core/states/Fates.html) 
harness that allows for exploring the possible paths through a test that has many decision points. For example:

```java
  @Test
  public void tryAllCombinations() {
      new Fates()
        .explore(decider -> {
          int a = decider.decide("a", new HashSet<>(Arrays.asList(1,2,3,4,5)));
          int b = decider.decide("b", new HashSet<>(Arrays.asList(5,4,3,2,1)));
          assertNotEquals(a, b);
        });
   }
```

This test has `5^2` possible values for `a` and `b`. The test will be run repeatedly until it either fails or
has tried all possible choices for a and b.

It's possible to substitute different algorithms for exploring the space of possible ways the test runs
by passing in a `StateExplorer` to the harness. For example there is a RandomExplorer than runs for a fixed
number of iterations.

# How it works

The harness launches your test in a separate JVM that has a custom java agent registered. This agent 
modifies the bytecode of all classes in the test (including JDK classes) to take control of where
threads are launched and where state is accessed or modified. Using this instrumentation, the 
harness creates a scheduler that only allows one thread to be running at a time. 

Each field access is used as a point where the scheduler can choose to let
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
 * Within the `fates-instrumentation` module:
   * `ExecutionEventSingleton` - this class has all of the events that the bytecode 
 instrumentation calls.
   * `AsmTransformer` - This class builds the pipeline of `ClassVisitors` that actually
 modify the user's bytecode


# Caveats

## Long running tests

Tests that access many fields lead to a large number of possible thread
orderings. This means that tests may take a *very* long time to complete.

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
