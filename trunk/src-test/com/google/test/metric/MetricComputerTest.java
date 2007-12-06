/*
 * Copyright 2007 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.test.metric;

import static com.google.test.metric.SignatureUtil.L;
import junit.framework.TestCase;

import java.util.List;

public class MetricComputerTest extends TestCase {

  private ClassRepository repo = new ClassRepository();
  private MetricComputer computer = new MetricComputer(repo);

  public static class Medium {
    public Medium() {
      statiCost1();
      cost2();
    }

    /**
     * I cost 1
     */
    public static int statiCost1() {
      int i = 0;
      return i > 0 ? 1 : 2;
    }

    /**
     * I cost 2, but I am instance method so I can be overridden. so my cost may
     * be avoided in most cases.
     */
    public int cost2() {
      int i = 0;
      return i > 0 ? i > 1 ? 1 : 2 : 2;
    }

    /**
     * I am instance method hence you will have to add the cost of constructor
     * to me. (by myself I cost 4)
     */
    public Object testMethod4() {
      int i = 0;
      i = i > 0 ? 1 : 2;
      i = i > 0 ? 1 : 2;
      i = i > 0 ? 1 : 2;
      i = i > 0 ? 1 : 2;
      return new Object();
    }
  }

  public void testMediumCost1() throws Exception {
    MethodInfo method = repo.getClass(Medium.class).getMethod("statiCost1()I");
    assertFalse(method.canOverride());
    MethodCost cost = computer.compute(Medium.class, "statiCost1()I");
    assertEquals(1l, cost.getComplexity());
  }

  /**
   * Since cost2 is called twice, once by our test and once by constructor we
   * don't want to add it twice. But the constructor adds 1 so total cost is 3.
   */
  public void testMediumCost2() throws Exception {
    MethodInfo method = repo.getClass(Medium.class).getMethod("cost2()I");
    assertTrue(method.canOverride());
    MethodCost cost = computer.compute(Medium.class, "cost2()I");
    assertEquals(3l, cost.getComplexity());
  }

  /**
   * Cost of the constructor needs to add the cost of the static method it calls
   * as it can not be overridden but not the cost of the instance method.
   */
  public void testMediumInit() throws Exception {
    MethodInfo method = repo.getClass(Medium.class).getMethod("<init>()V");
    assertFalse(method.canOverride());
    MethodCost cost = computer.compute(Medium.class, "<init>()V");
    assertEquals(1l, cost.getComplexity());
  }

  /**
   * Method4 is cost of 4 by itself, but one has the add the cost of constructor
   * since it is an instance method. The constructor is 0 but calls to methods:
   * cost1 method is static and can not be intercepted hence it has to be added.
   * cost2 method is instance and can be overridden hence we don't add that
   * cost.
   */
  public void testMediumMethod4() throws Exception {
    MethodCost cost = computer.compute(Medium.class,
        "testMethod4()Ljava/lang/Object;");
    assertEquals(5l, cost.getComplexity());
  }

  public static class Node {
    public String cost1() {
      int a = 0;
      return a == 2 ? "" : null;
    }
  }

  public static class Tree {
    private Node subTitle; // non-injectable
    public Node title = new Node(); // injectable (only after constructor)

    public Tree() {
    }

    public String titleLength() {
      return title.cost1();
    }

    public String subTitleLength() {
      return subTitle.cost1();
    }

    public String veryExpensive() {
      return "".toLowerCase();
    }
  }

  public void testTree() throws Exception {
    MethodCost cost = computer.compute(Tree.class, "<init>()V");
    assertEquals(0l, cost.getComplexity());
  }

  public void testTreeTitleLength() throws Exception {
    MethodCost cost = computer.compute(Tree.class,
        "titleLength()Ljava/lang/String;");
    assertEquals(0l, cost.getComplexity());
  }

  public void testTreeSubTitleLength() throws Exception {
    MethodCost cost = computer.compute(Tree.class,
        "subTitleLength()Ljava/lang/String;");
    assertEquals(1l, cost.getComplexity());
  }

  public void testVeryExpensive() throws Exception {
    MethodCost cost = computer.compute(Tree.class,
        "veryExpensive()Ljava/lang/String;");
    assertTrue(100l < cost.getComplexity());
  }

  static class ChoseConstructor {
    ChoseConstructor() {
    }

    ChoseConstructor(Object a) {
    }

    ChoseConstructor(Object a, int c, int d) {
    }

    ChoseConstructor(Object a, Object b) {
    }
  }

  public void testChooseConstructorWithMostParameters() throws Exception {
    ClassInfo classInfo = repo.getClass(ChoseConstructor.class);
    MethodInfo constructor = computer.getPrefferedConstructor(classInfo);
    assertEquals("<init>(Ljava/lang/Object;Ljava/lang/Object;)V", constructor
        .getNameDesc());
  }

  static class Singleton {
    private Singleton() {
      CostUtil.staticCost1();
    }

    public void doWork() {
      CostUtil.staticCost2();
    }
  }

  public void testIgnoreConstructorsIfAllConstructorsArePrivate()
      throws Exception {
    assertEquals(2L, computer.compute(Singleton.class, "doWork()V")
        .getComplexity());
  }

  static class StaticInit {
    static {
      CostUtil.staticCost1();
    }

    public void doWork() {
      CostUtil.staticCost2();
    }
  }

  public void testAddStaticInitializationCost() throws Exception {
    assertEquals(3L, computer.compute(StaticInit.class, "doWork()V")
        .getComplexity());
  }

  static class Setters {
    private Object o;

    public void setO(Object o) {
      this.o = o;
    }

    public void doWork() {
      o.toString();
    }
  }

  public void testSetterInjection() throws Exception {
    assertEquals(0L, computer.compute(Setters.class, "doWork()V")
        .getComplexity());
  }

  static class WholeClassCost {
    void methodA() {
      CostUtil.staticCost1();
    }

    void methodB() {
      CostUtil.staticCost1();
    }
  }

  public void testComputeClassCost() throws Exception {
    ClassCost cost = computer.compute(WholeClassCost.class);
    assertEquals(1L, cost.getMethodCost("methodA()V").getComplexity());
    assertEquals(1L, cost.getMethodCost("methodB()V").getComplexity());
  }

  static class Array {
    String[] strings;

    public void method() {
      strings.clone();
    }
  }

  public void testArray() throws Exception {
    repo.getClass(String[].class);
    computer.compute(repo.getClass(Array.class).getMethod("method()V"));
  }

  static class InjectableClass {
    public void cost4() {
      CostUtil.staticCost4();
    }

    public static void callCost0(InjectableClass ref) {
      indirection(ref);
    }

    private static void indirection(InjectableClass ref) {
      ref.cost4();
    }
    public static void callCost4() {
      InjectableClass x = new InjectableClass();
      indirection(x);
    }
  }

  public void testInjectabilityIsTransitive() throws Exception {
    ClassCost cost = computer.compute(InjectableClass.class);
    MethodCost callCost0 = cost.getMethodCost("callCost0("
        + L(InjectableClass.class) + ")V");
    MethodCost callCost4 = cost.getMethodCost("callCost4()V");
    assertEquals(0L, callCost0.getComplexity());
    assertEquals(4L, callCost4.getComplexity());
  }

  static class GlobalState {
    int i;
    static final String X = "X";

    public int inc() {
      return i++;
    }

    public void noop() {
    }

    @Override
    public String toString() {
      return X;
    }
  }

  static final GlobalState ref = new GlobalState();
  static int count;
  static class GlobalStateUser {

    // StateLoad: 0
    public void noLoad() {
    }

    // StateLoad: 1
    public void accessCount() {
      count++;
    }

    // StateLoad: 0
    public void accessFinalState() {
      ref.noop();
    }

    // StateLoad: 0
    public void accessFinalState2() {
      ref.toString();
    }

    // StateLoad: 1
    public void accessMutableState() {
      ref.inc();
    }
  }

  public void testGlobalLoadWhichAccessesFinalShouldBeZero() {
    ClassCost cost = computer.compute(GlobalState.class);
    MethodCost method = cost.getMethodCost("toString()Ljava/lang/String;");
    assertEquals(0L, method.getGlobal());
  }

  public void testGlobalLoadMethodDispatchNoStateAccessShouldBeZero() {
    ClassCost cost = computer.compute(GlobalStateUser.class);
    assertEquals(0L, cost.getMethodCost("noLoad()V").getGlobal());
    assertEquals(0L, cost.getMethodCost("accessFinalState()V").getGlobal());
    assertEquals(0L, cost.getMethodCost("accessFinalState2()V").getGlobal());
  }

  public void testGlobalLoadAccessStateShouldBeOne() {
    MethodCost cost = computer.compute(GlobalStateUser.class, "accessCount()V");
    assertEquals(1L, cost.getGlobal());
  }

  public void testGlobalLoadAccessStateThroughFinalShouldBeOne() {
    MethodCost cost = computer.compute(GlobalStateUser.class, "accessMutableState()V");
    assertEquals(1L, cost.getGlobal());
  }

  public void testJavaLangObjectParsesCorrectly() throws Exception {
    repo.getClass(Object.class);
  }

  public static class CostPerLine {
    static void main(){
      CostUtil.staticCost0();
      CostUtil.staticCost1();
      CostUtil.staticCost2();
    }
  }

  public void testCostPerLine() throws Exception {
    MethodCost cost = computer.compute(CostPerLine.class, "main()V");
    assertEquals(3, cost.getComplexity());
    List<LineNumberCost> lineNumberCosts = cost.getOperationCosts();
    assertEquals(4, lineNumberCosts.size());
    LineNumberCost line0 = lineNumberCosts.get(0); // the method - todo: although the cost for this is not as high as I'd expect ! (it is zero, shouldn't it be the sum of the other costs?!)
    LineNumberCost line1 = lineNumberCosts.get(1);
    LineNumberCost line2 = lineNumberCosts.get(2);
    LineNumberCost line3 = lineNumberCosts.get(3);

    assertEquals(0, line0.getCost());          // todo - this is failing, why? shouldn't the line0 (method) cost be the sum of the contained costs - the method calls w/in it? (= 0+1+2 = 3)?
    assertEquals(line0.getMethod().getStartingLineNumber() + 0, line0.getLineNumber());

    assertEquals(1, line1.getCost());
    assertEquals(line1.getMethod().getStartingLineNumber() + 1, line1.getLineNumber());

    assertEquals(2, line2.getCost());
    assertEquals(line2.getMethod().getStartingLineNumber() + 2, line2.getLineNumber());
  }

}