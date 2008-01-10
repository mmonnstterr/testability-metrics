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

import static java.lang.Integer.MAX_VALUE;
import static java.util.Collections.EMPTY_LIST;

import com.google.test.metric.asm.Visibility;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class HumanReadablePrinterTest extends AutoFieldClearTestCase {

  ClassInfo A = new ClassInfo("c.g.t.A", false, null, EMPTY_LIST);
  MethodInfo method0 =
    new MethodInfo(A, "method0", 1, "()V", null, null, null,
        Visibility.PUBLIC, 1, EMPTY_LIST);
  MethodInfo method1 =
    new MethodInfo(A, "method1", 1, "()V", null, null, null,
        Visibility.PUBLIC, 2, EMPTY_LIST);
  MethodInfo method2 =
    new MethodInfo(A, "method2", 2, "()V", null, null, null,
        Visibility.PUBLIC, 3, EMPTY_LIST);
  MethodInfo method3 =
    new MethodInfo(A, "method3", 2, "()V", null, null, null,
        Visibility.PUBLIC, 4, EMPTY_LIST);
  MethodCost cost0 = new MethodCost(method0);
  MethodCost cost1 = new MethodCost(method1);
  MethodCost cost2 = new MethodCost(method2);
  MethodCost cost3 = new MethodCost(method3);
  ByteArrayOutputStream out = new ByteArrayOutputStream();
  HumanReadablePrinter printer =
      new HumanReadablePrinter(new PrintStream(out));
  
  public void testSimpleCost() throws Exception {
    MethodCost cost = new MethodCost(method1);
    cost.addGlobalCost(0, null);
    cost.link();
    printer.print("", cost, Integer.MAX_VALUE, 0);
    assertEquals("c.g.t.A.method1()V[1, 1 / 1, 1]\n", out.toString());
  }
  
  public void testPrintTwoDeep() throws Exception {
    cost2.addMethodCost(81, new MethodCost(method1));
    cost2.link();
    printer.print("", cost2, MAX_VALUE, 0);
    assertEquals("c.g.t.A.method2()V[2, 0 / 3, 0]\n" +
        "  line 81: c.g.t.A.method1()V[1, 0 / 1, 0]\n", out.toString());
  }
  
  public void testThreeDeepPrint3() throws Exception {
    cost2.addMethodCost(8, cost1);
    cost3.addMethodCost(2, cost2);
    cost3.link();
    printer.print("", cost3, MAX_VALUE, 0);
    assertEquals("c.g.t.A.method3()V[3, 0 / 6, 0]\n" +
        "  line 2: c.g.t.A.method2()V[2, 0 / 3, 0]\n" +
        "    line 8: c.g.t.A.method1()V[1, 0 / 1, 0]\n", out.toString());
  }
  
  public void testTwoDeepSupressZeros() throws Exception {
    cost1.addMethodCost(8, cost0);
    cost1.link();
    printer.print("", cost1, MAX_VALUE, 0);
    assertEquals("c.g.t.A.method1()V[1, 0 / 1, 0]\n", out.toString());
  }
  
  public void testThreeDeepPrint2() throws Exception {
    cost2.addMethodCost(2, cost1);
    cost3.addMethodCost(2, cost2);
    cost3.link();
    printer.print("", cost3, 2, 0);
    assertEquals("c.g.t.A.method3()V[3, 0 / 6, 0]\n"
      + "  line 2: c.g.t.A.method2()V[2, 0 / 3, 0]\n", out.toString());
  }
  
  public void testSupressInsuficientCost100() throws Exception {
    cost2.addMethodCost(81, new MethodCost(method1));
    cost2.link();
    printer.print("", cost2, Integer.MAX_VALUE, 100);
    assertEquals("", out.toString());
  }
  
  public void testSupressInsuficientCost2() throws Exception {
    cost2.addMethodCost(81, new MethodCost(method1));
    cost2.link();
    printer.print("", cost2, Integer.MAX_VALUE, 2);
    assertEquals("c.g.t.A.method2()V[2, 0 / 3, 0]\n", out.toString());
  }
  
}
