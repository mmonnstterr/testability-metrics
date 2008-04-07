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

import com.google.classpath.ClasspathRootFactory;
import com.google.classpath.ClasspathRootGroup;
import com.google.classpath.ColonDelimitedStringParser;
import com.google.test.metric.report.DrillDownReport;
import com.google.test.metric.report.HtmlReport;
import com.google.test.metric.report.Report;
import com.google.test.metric.report.TextReport;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Testability {

  @Option(name = "-cp",
      usage = "colon delimited classpath to analyze (jars or directories)" +
          "\nEx. lib/one.jar:lib/two.jar")
  protected String cp = System.getProperty("java.class.path", ".");

  @Option(name = "-printDepth",
      usage = "Maximum depth to recurse and print costs of classes/methods " +
      	  "that the classes under analysis depend on. Defaults to 0.")
  int printDepth = 2;

  @Option(name = "-minCost",
      usage = "Minimum Total Class cost required to print that class' metrics.")
  int minCost = 1;

  @Option(name = "-maxExcellentCost",
      usage = "Maximum Total Class cost to be classify it as 'excellent'.")
  int maxExcellentCost = 50;

  @Option(name = "-worstOffenderCount",
      usage = "Print N number of worst offending classes.")
  public
  int worstOffenderCount = 20;

  @Option(name = "-maxAcceptableCost",
      usage = "Maximum Total Class cost to be classify it as 'acceptable'.")
  int maxAcceptableCost = 100;

  @Option(name = "-whitelist",
          usage = "colon delimited whitelisted packages that will not " +
                  "count against you. Matches packages/classes starting with " +
                  "given values. (Always whitelists java.*. RegExp OK.)")
  String wl = null;
  private final RegExpWhiteList whitelist = new RegExpWhiteList();

  @Option(name = "-print",
      usage = "summary: (default) print package summary information.\n" +
              "detail: print detail drill down information for each method call.")
  String printer = "summary";

  @Option(name = "cyclomatic",
      metaVar = "cyclomatic cost multiplier",
      usage = "When computing the overall cost of the method the " +
              "individual costs are added using weighted average. " +
              "This represents the weight of the cyclomatic cost.")
  double cyclomaticMultiplier = 1;

  @Option(name = "global",
      metaVar = "global state cost multiplier",
      usage = "When computing the overall cost of the method the " +
          "individual costs are added using weighted average. " +
          "This represents the weight of the global state cost.")
  double globalMultiplier = 10;

  @Argument(metaVar = "classes and packages",
          usage = "Classes or packages to analyze. " +
          "Matches any class starting with these.\n" +
          "Ex. com.example.analyze.these com.google.and.these.packages " +
          "com.google.AClass", required = true)
  protected List<String> entryList = new ArrayList<String>();
  protected ClasspathRootGroup classpath;

  private final PrintStream out;
  private final PrintStream err;

  private Report report;

  public Testability(PrintStream out, PrintStream err) {
    this.out = out;
    this.err = err;
    this.whitelist.addPackage("java.");
  }

  public static void main(String... args) {
    main(System.out, System.err, args);
  }

  public static void main(PrintStream out, PrintStream err, String... args) {
    Testability testability = new Testability(out, err);
    try {
      testability.parseArgs(args);
      testability.execute();
    } catch (CmdLineException ignored) { }
  }

  public void parseArgs(String... args) throws CmdLineException {
    CmdLineParser parser = new CmdLineParser(this);
    try {
      parser.parseArgument(args);
      if (entryList.isEmpty()) {
        throw new CmdLineException("No argument was given");
      } else {
        for (int i = 0; i < entryList.size(); i++) {
          entryList.set(i, entryList.get(i).replaceAll("/", "."));
        }
      }
    } catch (CmdLineException e) {
      err.println(e.getMessage() + "\n");
      parser.setUsageWidth(120);
      parser.printUsage(err);
      throw new CmdLineException("Exiting...");
    }
  }

  private void postParse() throws CmdLineException {
    for (String packageName : new ColonDelimitedStringParser(wl).getStrings()) {
        whitelist.addPackage(packageName);
    }
    if (entryList.isEmpty()) {
      entryList.add("");
    }
    classpath = ClasspathRootFactory.makeClasspathRootGroup(cp);
    if (printer.equals("summary")) {
      report = new TextReport(out, maxExcellentCost, maxAcceptableCost, worstOffenderCount);
    } else if (printer.equals("html")) {
      report = new HtmlReport(out, maxExcellentCost, maxAcceptableCost, worstOffenderCount);
    } else if (printer.equals("detail")) {
      report = new DrillDownReport(out, entryList, printDepth, minCost);
    } else {
      throw new CmdLineException("Don't understand '-print' option '"
          + printer + "'");
    }
  }

  public void execute() throws CmdLineException {
    postParse();
    ClassRepository repository = new ClassRepository(classpath);
    CostModel costModel = new CostModel(cyclomaticMultiplier, globalMultiplier);
    MetricComputer computer = new MetricComputer(repository, err, whitelist, costModel);
    List<String> classNames = classpath.getClassNamesToEnter(entryList);
    report.printHeader();
    for (String className : classNames) {
      try {
        ClassCost classCost = computer.compute(repository.getClass(className));
        report.addClassCost(classCost);
      } catch (ClassNotFoundException e) {
        err.println("WARNING: can not analyze class '" + className +
            "' since class '" + e.getClassName() + "' was not found.");
      }
    }
    report.printFooter();
  }
}
