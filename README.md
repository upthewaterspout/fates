FATES stands for Find All Thread Execution Schedules. It is a framework for
testing multi-threaded Java applications. It instruments the bytecode of the
program under test and takes control of thread scheduling. It then runs the
test repeatedly until all possible scheduling orders are tested.

This framework is in very early stages of development, and will not
successfully run for anything but very trivial cases.

# How to use
1. Add fates-all.jar as a javaagent to the command line when you run your tests.

```
java -java-agent:fates-all.jar ...
```

In your test code, run your multithreaded test using the Fates class, like so
```java
@Test
public void findRace() {
  Fates.run(() -> {
    //Your test code here
  });
}
```

See the [javadocs](https://upthewaterspout.github.io/fates/javadoc/) for more
information. The two useful classes from a user perspective are:
* [Fates](https://upthewaterspout.github.io/fates/javadoc/com/github/upthewaterspout/fates/core/threading/Fates.html)
The main harness for running multithreaded tests
* [StateExplorationHarness](https://upthewaterspout.github.io/fates/javadoc/com/github/upthewaterspout/fates/core/states/StateExplorationHarness.html)
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
 * `StateExplorationHarness` and the `states` package - this package contains all
 of the logic to execute a test multiple times and explore all of the possible
 choices a test might make. This harness may be useful in other contexts. For
 example instead of using junit's parameterized tests, a test could simply ask
 the decider to choose between a set of parameters and the test will be run
 once for each choice.

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
during classloading. That means we may miss race conditions that occur during
classloading. But without this, a second run through the same path won't load
classes, so it won't be the same path anymore.

We could start with a fresh classloader for each run, but we can't replay the
loading of JDK classes as far as I can tell, so we still have a problem. Maybe
a combination of a fresh classloader and treating the loading of JDK classes as
atomic would work.

## Blocked thread handling

LockSupport, synchronization, wait, notify, etc. are all handled by the
scheduler. But there could be other events that cause a thread to block, for
example a blocking IO operation that depends on another thread. These
operations will currently just cause the test to hang.

## Timed waits

Timed wait calls are currently no ops, because it's possible the thread in a timed
wait could pick up and continue without other threads running. 
