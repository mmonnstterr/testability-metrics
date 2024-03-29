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
package com.google.test.metric.method.op.turing;

import com.google.test.metric.MethodInfo;
import com.google.test.metric.TestabilityContext;
import com.google.test.metric.Variable;

public class ReturnOperation extends Operation {

  private final Variable value;

  public ReturnOperation(int lineNumber, Variable value) {
    super(lineNumber);
    this.value = value;
  }

  @Override
  public void computeMetric(TestabilityContext context, MethodInfo currentMethod) {
    context.setReturnValue(value);
  }

  @Override
  public String toString() {
    return "return " + value;
  }

}
