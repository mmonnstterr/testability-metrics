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

public class MethodCost {

  private final long complexity;
  private final MethodInfo method;

  public MethodCost(MethodInfo method, long totalCost) {
    this.method = method;
    this.complexity = totalCost;
  }

  public long getComplexity() {
    return complexity;
  }

  public String getNameDesc() {
    return method.getNameDesc();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    toString(buf);
    return buf.toString();
  }

  public void toString(StringBuilder buf) {
    buf.append(method.getNameDesc());
    buf.append(" cost: ");
    buf.append(complexity);
  }

}
