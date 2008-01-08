
Testability Metric
Introduction

There are a lot of metrics out there which do static analysis on the code an try to come up with a number of "goodness". So why do we need another one? Well there are two reasons for that: (1) To get one more perspective on your code base and (2) to try to influence/educate people on the benefits of writhing testable code.
Basic Idea

The basic idea of testability score is that it tries to represent the difficulty of testing a particular piece of code. The difficulty is a measure of (1) how hard will it be to construct this object (2) how hard will it be to run the method in order to be able to assert something in a test.
Simplest Example

SOURCE:

public class Primeness {
  public boolean isPrime(int number) {
    for (int i = 2; i < number / 2; i++) {
      if (number % i == 0) {
        return false;
      }
    }
    return true;
  }
}

TRY IT:

    testability.sh com.google.test.metric.example.Primeness

OUTPUT:

com.google.test.metric.example.Primeness
  <init>()V[0, 0 / 0, 0]
  isPrime(I)Z[2, 0 / 2, 0]

EXPLANATION:

com.google.test.metric.example.Primeness
                            | Name of the class
  <init>()V[0, 0 / 0, 0]    | Name of the method under test
                            |   The <init> notation refers to
                            |   constructor.
                            | The [0, 0 / 0, 0] refers to the metric
                            | [tc, gc / ttc, tgc]
                            | (tc,gc) is per method cost
                            | (ttc, tgc) total/recursive cost
                            | tc: test complexity
                            | gc: global state complexity.
                            | ttc: Total test complexity (recursive)
                            | tgc: Total global complexity (recursive)
  isPrime(I)Z[2, 0 / 2, 0]  | This method has testing cost of 2
                            | and has no global statics

Test Complexity

In the above example we got a test complexity of 2. This is because the method is Prime has a loop and an if statement. Therefore there are 2 additional paths of execution for a total of 3.

   1. Loop does not execute
   2. Loop executes but if does not evaluate to true
   3. Loop executes and if evaluates to true.

Note: this is just cyclomatic complexity of the method minus one. The reason we are subtracting one is that we don't want to penalize for breaking the method into 2 smaller methods. If the lowest cost would be 1, splitting the method into two would result in the cost of 2. Having the simplest method be 0 allows us to split the method to smaller functions which all have the cost of 0. Hence the user is not penalized for breaking methods into smaller ones, which is a good idea.
Example: Injectibility Scoring

This example shows the differences in scores based on how injectible a class is. In SumOfPrimes, it instantiates a new Primeness() directly. The new operator prevents you from being able to inject in a different Primeness for testing. Thus the scores differ.

    * sum(I)I[2, 0 / 4, 0] <- total test complexity of 4 for SumOfPrimes
    * sum(I)I[2, 0 / 2, 0] <- total test complexity of 2 for SumOfPrimes2

SOURCE:

public class SumOfPrimes {

  private Primeness primeness = new Primeness();

  public int sum(int max) {
    int sum = 0;
    for (int i = 0; i < max; i++) {
      if (primeness.isPrime(i)) {
        sum += i;
      }
    }
    return sum;
  }

}

OUTPUT:

com.google.test.metric.example.SumOfPrimes
  <init>()V[0, 0 / 0, 0]
  sum(I)I[2, 0 / 4, 0]
    line 25: isPrime(I)Z[2, 0 / 2, 0]

SOURCE:

public class SumOfPrimes2 {

  private final Primeness primeness;

  public SumOfPrimes2(Primeness primeness) {
    this.primeness = primeness;
  }

  public int sum(int max) {
    int sum = 0;
    for (int i = 0; i < max; i++) {
      if (primeness.isPrime(i)) {
        sum += i;
      }
    }
    return sum;
  }

}

OUTPUT:

com.google.test.metric.example.SumOfPrimes2
  <init>(Lcom/google/test/metric/example/Primeness;)V[0, 0 / 0, 0]
  sum(I)I[2, 0 / 2, 0]

Example: Global State
Example: ...
Future Enhancements / Requests

Please talk about what you want on the mailing list: http://groups.google.com/group/testability-metrics