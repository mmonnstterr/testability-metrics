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

import com.google.classpath.DirectoryClasspathRootTest;
import com.google.classpath.JarClasspathRootTest;
import org.kohsuke.args4j.CmdLineException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TestabilityTest extends AutoFieldClearTestCase {
  private WatchedOutputStream out;
  private WatchedOutputStream err;
  private Testability testability;

  static final String ANT_OUTPUT_DIR = "build/classes";
  static final String IDEA_OUTPUT_DIR = "out/production/testability-metrics";
  static final String ECLIPSE_OUTPUT_DIR = "bin/";

  /**
   * The output directory this project compiles to by default. For compatibility,
   * this defaults to the ant output directory.
   */
  private static String whereToLookForClasses;

  @Override
  protected void setUp() {
    whereToLookForClasses = ANT_OUTPUT_DIR;
    out = new WatchedOutputStream();
    err = new WatchedOutputStream();
    testability = new Testability(new PrintStream(out), new PrintStream(err));
  }

  public void testComputeCostSingleClass() throws Exception {
    String classUnderTest = com.google.test.metric.Testability.class.getName();
    ClassCost classCost = testability.computeCost(classUnderTest);
    assertNotNull(classCost);
    assertTrue(classCost.toString().length() > 0);
  }

  public void testParseNoArgs() throws IOException {
    try {
      testability.parseArgs();
      fail("Should have thrown a CmdLineException exception");
    } catch (CmdLineException expected) {
      // expecting this
    }
    assertTrue(err.toString().indexOf("Argument \"classes/packages\" is required") > -1);
  }

  public void testParseClasspathAndSingleClass() throws Exception {
    testability.parseArgs("-cp", "not/default/path", "com.google.TestClass");

    assertEquals("", err.getOutput());
    assertEquals("not/default/path", testability.classpath);
    List<String> expectedArgs = new ArrayList<String>();
    expectedArgs.add("com.google.TestClass");
    assertNotNull(testability.entryList);
    assertEquals(expectedArgs, testability.entryList);
  }


  public void testJarFileNoClasspath() throws Exception {
    testability.run("junit.runner", "-cp");
    /** we expect the error to say something about proper usage of the arguments.
     * The -cp needs a value */
    assertTrue(out.getOutput().length() == 0);
    assertTrue(err.getOutput().length() > 0);
  }


  public void testJarFileParseSetupAndComputeGroupMetric() throws Exception {
    testability.parseSetup("", "-cp", JarClasspathRootTest.JUNIT_JAR);
    testability.computeGroupMetric();
    assertTrue("output too short, expected parsing lots of files correctly",
        out.getOutput().length() > 1000);
    assertEquals(1, testability.entryList.size());
    assertEquals("", testability.entryList.get(0));
    assertTrue(testability.classpath.endsWith(JarClasspathRootTest.JUNIT_JAR));
  }

  public void testClassesNotInClasspath() throws Exception {
    /* root2/ contains some classes from this project, but not all. There are
     * many references to classes that will not be in this test's -cp classpath.
     * We are testing that when the ClassRepository encounters a
     * ClassNotFoundException, it continues nicely and prints the values for
     * the classes that it _does_ find. */
    testability.run("", "-cp", DirectoryClasspathRootTest.ROOT_2_CLASSES_FOR_TEST);
    assertTrue(out.getOutput().length() > 0);
    assertTrue(err.getOutput().length() > 0);
  }

  public void testJarFileAndJunitSwinguiProgressBarEntryPattern() throws Exception {
    testability.run("junit.swingui.ProgressBar", "-cp", JarClasspathRootTest.JUNIT_JAR);
    assertTrue(out.getOutput().length() > 0);
    assertTrue(err.getOutput().length() == 0);
  }

  public void testJarFileAndJunitRunnerEntryPattern() throws IOException {
    testability.run("junit.runner", "-cp", JarClasspathRootTest.JUNIT_JAR);
    assertTrue(out.getOutput().length() > 0);
    assertTrue(err.getOutput().length() == 0);
    System.out.println(out.getOutput());
  }

  public void testJarFileAndJunitRunnerEntryPatternAndMaxDepthTwo() throws IOException {
    testability.run("junit.runner", "-cp", JarClasspathRootTest.JUNIT_JAR, "-maxPrintingDepth", "2");
    assertTrue(out.getOutput().length() > 0);

    Pattern sixSpacesThenLinePattern = Pattern.compile("^(\\s){6}line", Pattern.MULTILINE);
    assertTrue("Expected 6 leading spaces spaces for maxPrintingDepth=2",
        sixSpacesThenLinePattern.matcher(out.getOutput()).find());

    Pattern over7SpacesThenLinePattern = Pattern.compile("^(\\s){7,}line", Pattern.MULTILINE);
    assertFalse("Should not have had more than 2 + 2*2 = 6 leading spaces for maxPrintingDepth=2",
        over7SpacesThenLinePattern.matcher(out.getOutput()).find());
    assertTrue(err.getOutput().length() == 0);
  }

  public void testJarFileAndJunitRunnerEntryPatternAndMaxDepthZero() throws IOException {
    testability.run("junit.runner", "-cp", JarClasspathRootTest.JUNIT_JAR, "-maxPrintingDepth", "0");
    assertTrue(out.getOutput().length() > 0);

    Pattern noLinesPattern = Pattern.compile("^(\\s)*line", Pattern.MULTILINE);
    assertFalse("Should not have any line matchings for printing depth of 0",
        noLinesPattern.matcher(out.getOutput()).find());
    assertEquals(0, err.getOutput().length());
  }

  public void testJarsAndDirectoryWildcardEntryPattern() throws Exception {
    testability.run("" /* blank will look for everything */, "-cp",
        JarClasspathRootTest.ASM_JAR + ":" + JarClasspathRootTest.JUNIT_JAR + ":" + whereToLookForClasses);
    assertTrue(out.getOutput().length() > 0);
    assertEquals(0, err.getOutput().length());
  }

  public void testIncompleteClasspath() throws Exception {
    testability.run("" /* blank will look for everything */, "-cp", whereToLookForClasses);
    assertTrue("Output was empty, some output expected", out.getOutput().length() > 0);
    assertTrue("Error output was empty, expected error output from class not found",
        err.getOutput().length() > 0);
  }

  public void testMainWithJarsAndDirectoryOfClassesAndFilter() throws Exception {
    testability.run("junit.swingui.ProgressBar", "-cp",
        JarClasspathRootTest.JUNIT_JAR + ":" + whereToLookForClasses);
    assertTrue(out.getOutput().length() > 0);
    assertEquals(0, err.getOutput().length());
  }

  public void testForWarningWhenClassesRecurseToIncludeClassesOutOfClasspath() throws Exception {
    testability.run("" /* blank will look for everything */, "-cp", whereToLookForClasses);
    assertTrue("Output was empty, some output expected", out.getOutput().length() > 0);
    assertTrue("Error output was empty, expected error output from class not found",
        err.getOutput().length() > 0);
    assertTrue(err.getOutput().indexOf("WARNING: class not found") > -1);
  }

  public void testForWarningWhenClassExtendsFromClassOutOfClasspath() throws Exception {
    testability.computeCost("ThisClassDoesNotExist");
    assertEquals(0, out.getOutput().length());
    assertTrue(err.getOutput().length() > 0);
    assertTrue(err.getOutput().startsWith("WARNING: can not analyze class 'ThisClassDoesNotExist"));
  }

  public void testFilterCostOverTotalCostThreshold() throws Exception {
    testability.run("junit.runner", "-cp", JarClasspathRootTest.JUNIT_JAR);
    int baselineLength = out.getOutput().length();
    testability.run("junit.runner", "-cp", JarClasspathRootTest.JUNIT_JAR, "-costThreshold", "1000");
    int throttledLength = out.getOutput().length();
    assertTrue(baselineLength < throttledLength);
  }

  public static class WatchedOutputStream extends OutputStream {
    StringBuffer sb = new StringBuffer();

    @Override
    public void write(int ch) throws IOException {
      sb.append(ch);
    }

    @Override
    public void write(byte[] b) throws IOException {
      sb.append(new String(b));
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      sb.append(new String(b, off, len));
    }

    public String getOutput() {
      return sb.toString();
    }

    public String toString() {
      return sb.toString();
    }
  }
}
