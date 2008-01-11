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
package com.google.test.metric.method;

import com.google.test.metric.FieldInfo;
import com.google.test.metric.Type;
import com.google.test.metric.Variable;
import com.google.test.metric.method.op.stack.JSR;
import com.google.test.metric.method.op.stack.Load;
import com.google.test.metric.method.op.stack.PutField;
import com.google.test.metric.method.op.stack.Return;
import com.google.test.metric.method.op.turing.Operation;

import junit.framework.TestCase;

import java.util.List;

public class Stack2TuringTest extends TestCase {

  public void testJSRSingleBlock() throws Exception {
    Block main = new Block("main");
    Block sub = new Block("sub");

    main.addOp(new Load(0, new Variable("this", Type.OBJECT, false, false)));
    main.addOp(new JSR(0, sub));
    main.addOp(new PutField(0, new FieldInfo(null, "a", Type.INT, false, false,
        false)));

    sub.addOp(new Load(0, new Constant(1, Type.INT)));
    sub.addOp(new Return(0, Type.VOID));

    Stack2Turing converter = new Stack2Turing(main);
    List<Operation> operations = converter.translate();
    assertEquals(1, operations.size());
    assertEquals("null.a{int} <- 1{int}", operations.get(0).toString());
  }

  public void testJSRMultiBlock() throws Exception {
    Block main = new Block("main");
    Block sub = new Block("sub");
    Block sub1 = new Block("sub1");
    Block sub2 = new Block("sub2");
    sub.addNextBlock(sub1);
    sub1.addNextBlock(sub2);

    main.addOp(new JSR(0, sub));

    Stack2Turing converter = new Stack2Turing(main);
    converter.translate(); // Assert no exceptions
  }

}
