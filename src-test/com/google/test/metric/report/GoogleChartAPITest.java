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

import junit.framework.TestCase;

public class GoogleChartAPITest extends TestCase {

  public void testUrlEncoding() throws Exception {
    GoogleChartAPI api = new GoogleChartAPI();
    api.getMap().put("a", "b c");
    assertEquals("http://chart.apis.google.com/chart?a=b+c", api.toString());
  }

  public void testEncodeS() throws Exception {
    GoogleChartAPI api = new GoogleChartAPI();
    assertEquals('A', api.encodeS(0));
    assertEquals('Z', api.encodeS(25));
    assertEquals('a', api.encodeS(26));
    assertEquals('z', api.encodeS(51));
    assertEquals('0', api.encodeS(52));
    assertEquals('9', api.encodeS(61));
    assertEquals('9', api.encodeS(62));
    assertEquals('9', api.encodeS(100));
  }

}
