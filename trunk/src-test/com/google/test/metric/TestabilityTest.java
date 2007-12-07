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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.kohsuke.args4j.CmdLineException;

public class TestabilityTest extends TestCase {

  public static class IgnoreOutputStream extends OutputStream {

    @Override
    public void write(int ch) throws IOException {
    }
    
    @Override
    public void write(byte[] b) throws IOException {
    }
    
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
    }

  }

  public void testParseClasspathAndSingleClass() throws Exception {
    Testability testability = new Testability();
    StringWriter err = new StringWriter();
    testability.parseArgs(err, "-cp", "not/default/path",
        "com.google.TestClass");

    assertEquals("", err.toString());
    assertEquals("not/default/path", testability.getClasspath());
    List<String> expectedArgs = new ArrayList<String>();
    expectedArgs.add("com.google.TestClass");
    assertNotNull(testability.getArguments());
    assertEquals(expectedArgs, testability.getArguments());
  }

  public void testComputeCostSingleClass() throws Exception {
    // this is how the underlying application computes the cost, however
    // the CLI does not support testing a single class
    String classUnderTest = com.google.test.metric.Testability.class.getName();
    Testability testability = new Testability();
    ClassCost classCost = testability.computeCost(classUnderTest);
    assertNotNull(classCost);
    assertTrue(classCost.toString().length() > 0);
  }

  public void testMainWithDirectoryOfClasses() throws Exception {
    Testability
        .main("classes/production/testability-metrics", "-cp",
            "lib/asm-3.0.jar:lib/args4j-2.0.8.jar:classes/production/testability-metrics");
  }

  public void testMainWithJarFile() throws Exception {
    PrintStream out = System.out;
    try {
      System.setOut(new PrintStream(new IgnoreOutputStream()));
      Testability.main("lib/junit.jar", "-cp", "lib/junit.jar");
    } finally {
      System.setOut(out);
    }
  }

  public void testMainWithJarFileNoClasspath() throws Exception {
    PrintStream out = System.out;
    try {
      System.setOut(new PrintStream(new IgnoreOutputStream()));
      Testability.main("lib/junit.jar");
    } finally {
      System.setOut(out);
    }
  }

  public void testParseNoArgs() throws IOException {
    Testability testability = new Testability();
    StringWriter err = new StringWriter();
    try {
      testability.parseArgs(err);
      fail("Should have thrown a CmdLineException exception");
    } catch (CmdLineException expected) {
      // expecting this
    }
    assertTrue(err.toString().indexOf("No argument was given") > -1);
  }
}
