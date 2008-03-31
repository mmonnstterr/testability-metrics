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
package com.google.test.metric.report;

import static com.google.test.metric.report.DrillDownReport.NEW_LINE;
import static java.lang.Integer.MAX_VALUE;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.google.test.metric.AutoFieldClearTestCase;
import com.google.test.metric.ClassCost;
import com.google.test.metric.CostModel;
import com.google.test.metric.MethodCost;

public class DrillDownReportTest extends AutoFieldClearTestCase {

  private final  MethodCost methodCost0 = new MethodCost("c.g.t.A.method0()V", 0, 0);
  private final  MethodCost methodCost1 = new MethodCost("c.g.t.A.method1()V", 0, 1);
  private final  MethodCost methodCost2 = new MethodCost("c.g.t.A.method2()V", 0, 2);
  private final  MethodCost methodCost3 = new MethodCost("c.g.t.A.method3()V", 0, 3);
  private final ByteArrayOutputStream out = new ByteArrayOutputStream();
  private final CostModel context = new CostModel();

  public void testSimpleCost() throws Exception {
    DrillDownReport printer =
      new DrillDownReport(new PrintStream(out), null, MAX_VALUE, 0);
    MethodCost costOnlyMethod1 = new MethodCost("c.g.t.A.method1()V", 0, 1);
    costOnlyMethod1.addGlobalCost(0, null);
    costOnlyMethod1.link(context);
    printer.print("", costOnlyMethod1, Integer.MAX_VALUE);
    assertStringEquals("c.g.t.A.method1()V [1, 1 / 1, 1]\n", out.toString());
  }

  public void test2DeepPrintAll() throws Exception {
    DrillDownReport printer =
      new DrillDownReport(new PrintStream(out), null, MAX_VALUE, 0);
    methodCost2.addMethodCost(81, new MethodCost("c.g.t.A.method1()V", 0, 1));
    methodCost2.link(context);
    printer.print("", methodCost2, MAX_VALUE);
    assertStringEquals("c.g.t.A.method2()V [2, 0 / 3, 0]\n" +
        "  line 81: c.g.t.A.method1()V [1, 0 / 1, 0]\n", out.toString());
  }

  public void test3DeepPrintAll() throws Exception {
    DrillDownReport printer =
      new DrillDownReport(new PrintStream(out), null, MAX_VALUE, 0);
    methodCost2.addMethodCost(8, methodCost1);
    methodCost3.addMethodCost(2, methodCost2);
    methodCost3.link(context);
    printer.print("", methodCost3, MAX_VALUE);
    assertStringEquals("c.g.t.A.method3()V [3, 0 / 6, 0]\n" +
        "  line 2: c.g.t.A.method2()V [2, 0 / 3, 0]\n" +
        "    line 8: c.g.t.A.method1()V [1, 0 / 1, 0]\n", out.toString());
  }

  public void test2DeepSupress0Cost() throws Exception {
    DrillDownReport printer =
      new DrillDownReport(new PrintStream(out), null, MAX_VALUE, 2);
    methodCost1.addMethodCost(8, methodCost0);
    methodCost1.addMethodCost(13, methodCost3);
    methodCost1.link(context);
    printer.print("", methodCost1, MAX_VALUE);
    assertStringEquals("c.g.t.A.method1()V [1, 0 / 4, 0]\n" +
    		"  line 13: c.g.t.A.method3()V [3, 0 / 3, 0]\n", out.toString());
  }

  public void test3DeepPrint2Deep() throws Exception {
    DrillDownReport printer =
      new DrillDownReport(new PrintStream(out), null, MAX_VALUE, 0);
    methodCost3.addMethodCost(2, methodCost2);
    methodCost2.addMethodCost(2, methodCost1);
    methodCost3.link(context);
    printer.print("", methodCost3, 2);
    assertStringEquals("c.g.t.A.method3()V [3, 0 / 6, 0]\n"
      + "  line 2: c.g.t.A.method2()V [2, 0 / 3, 0]\n", out.toString());
  }

  public void testSupressAllWhenMinCostIs4() throws Exception {
    DrillDownReport printer =
      new DrillDownReport(new PrintStream(out), null, MAX_VALUE, 4);
    methodCost2.addMethodCost(81, new MethodCost("c.g.t.A.method1()V", 0, 1));
    methodCost2.link(context);
    printer.print("", methodCost2, MAX_VALUE);
    assertStringEquals("", out.toString());
  }

  public void testSupressPartialWhenMinCostIs2() throws Exception {
    DrillDownReport printer =
      new DrillDownReport(new PrintStream(out), null, MAX_VALUE, 2);
    methodCost2.addMethodCost(81, new MethodCost("c.g.t.A.method1()V", 0, 1));
    methodCost2.link(context);
    printer.print("", methodCost2, Integer.MAX_VALUE);
    assertStringEquals("c.g.t.A.method2()V [2, 0 / 3, 0]\n", out.toString());
  }

  public void testSecondLevelRecursive() throws Exception {
    DrillDownReport printer =
      new DrillDownReport(new PrintStream(out), null, MAX_VALUE, 0);
    methodCost3.addMethodCost(1, methodCost2);
    methodCost2.addMethodCost(2, methodCost2);
    methodCost3.link(context);
    printer.print("", methodCost3, 10);
    assertStringEquals("c.g.t.A.method3()V [3, 0 / 5, 0]\n"
      + "  line 1: c.g.t.A.method2()V [2, 0 / 2, 0]\n", out.toString());
  }

  public void testAddOneClassCostThenPrintIt() throws Exception {
    DrillDownReport printer =
      new DrillDownReport(new PrintStream(out), null, MAX_VALUE, 0);
    ClassCost classCost0 = new ClassCost("FAKE_classInfo0", new ArrayList<MethodCost>());
    printer.addClassCost(classCost0);
    printer.printFooter();
    assertStringEquals("\nTestability cost for FAKE_classInfo0 [ cost = 0 ] [ 0 TCC, 0 TGC ]\n",
        out.toString());
  }

  public void testAddSeveralClassCostsAndPrintThem() throws Exception {
    DrillDownReport printer =
      new DrillDownReport(new PrintStream(out), null, MAX_VALUE, 0);
    ClassCost classCost0 = new ClassCost("FAKE_classInfo0", new ArrayList<MethodCost>());
    ClassCost classCost1 = new ClassCost("FAKE_classInfo1", new ArrayList<MethodCost>());
    ClassCost classCost2 = new ClassCost("FAKE_classInfo2", new ArrayList<MethodCost>());
    printer.addClassCost(classCost0);
    printer.addClassCost(classCost1);
    printer.addClassCost(classCost2);
    printer.printFooter();
    assertStringEquals("\nTestability cost for FAKE_classInfo0 [ cost = 0 ] [ 0 TCC, 0 TGC ]\n" +
        "\nTestability cost for FAKE_classInfo1 [ cost = 0 ] [ 0 TCC, 0 TGC ]\n" +
        "\nTestability cost for FAKE_classInfo2 [ cost = 0 ] [ 0 TCC, 0 TGC ]\n",
        out.toString());
  }

  public void testAddSeveralClassCostsAndPrintThemInDescendingCostOrder()
      throws Exception {
    DrillDownReport printer =
      new DrillDownReport(new PrintStream(out), null, MAX_VALUE, 0);
    List<MethodCost> methodCosts1 = new ArrayList<MethodCost>();
    methodCosts1.add(methodCost1);
    List<MethodCost> methodCosts2 = new ArrayList<MethodCost>();
    methodCosts2.add(methodCost2);
    ClassCost classCost0 = new ClassCost("FAKE_classInfo0", new ArrayList<MethodCost>());
    ClassCost classCost1 = new ClassCost("FAKE_classInfo1", methodCosts1);
    ClassCost classCost2 = new ClassCost("FAKE_classInfo2", methodCosts2);
    classCost0.link(context);
    classCost1.link(context);
    classCost2.link(context);
    printer.addClassCost(classCost0);
    printer.addClassCost(classCost1);
    printer.addClassCost(classCost2);
    printer.printFooter();
    assertStringEquals("\nTestability cost for FAKE_classInfo2 [ cost = 2 ] [ 2 TCC, 0 TGC ]\n" +
    		"  c.g.t.A.method2()V [2, 0 / 2, 0]\n" +
        "\nTestability cost for FAKE_classInfo1 [ cost = 1 ] [ 1 TCC, 0 TGC ]\n" +
        "  c.g.t.A.method1()V [1, 0 / 1, 0]\n" +
        "\nTestability cost for FAKE_classInfo0 [ cost = 0 ] [ 0 TCC, 0 TGC ]\n",
        out.toString());
  }

	private void assertStringEquals(String expected, String actual) {
		assertEquals(expected.replace("\n", NEW_LINE), actual);
	}

}
