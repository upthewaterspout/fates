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
Add fates-threads-0.1-SNAPSHOT.jar as a javaagent to the command line when you run your tests.

```
java -javaagent:fates-threads-0.1-SNAPSHOT.jar ...
```

In your test code, run your multithreaded test using the Fates class. For example, using junit:
```java
@Test
public void findRace() {
  ThreadFates.run(() -> {
    //Your mutlithreaded test
  });
}
```

See the [javadocs](https://upthewaterspout.github.io/fates/javadoc/) for more
information. The two useful classes from a user perspective are:
* [ThreadFates](https://upthewaterspout.github.io/fates/javadoc/com/github/upthewaterspout/fates/core/threading/ThreadFates.html)
The main harness for running multithreaded tests
* [Fates](https://upthewaterspout.github.io/fates/javadoc/com/github/upthewaterspout/fates/core/states/Fates.html)
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
 * THe `fates-explore` module with the `Fates` class and the `states` package - this package contains 
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
