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
package com.google.test.metric.method.op.stack;

import com.google.test.metric.FieldInfo;
import com.google.test.metric.Variable;
import com.google.test.metric.method.op.turing.Assignment;
import com.google.test.metric.method.op.turing.Operation;

import java.util.List;

public class PutField extends StackOperation {

  private final FieldInfo fieldInfo;

  public PutField(int lineNumber, FieldInfo fieldInfo) {
    super(lineNumber);
    this.fieldInfo = fieldInfo;
  }

  @Override
  public String toString() {
    return "put " + (fieldInfo.isStatic() ? "static " : "") + fieldInfo;
  }

  @Override
  public int getOperatorCount() {
    int valueCount = fieldInfo.getType().isDouble() ? 2 : 1;
    int fieldThis = fieldInfo.isStatic() ? 0 : 1;
    return valueCount + fieldThis;
  }

  @Override
  public Operation toOperation(List<Variable> input) {
    Variable variable = input.get(fieldInfo.isStatic() ? 0 : 1);
    return new Assignment(lineNumber, fieldInfo, variable);
  }

}
