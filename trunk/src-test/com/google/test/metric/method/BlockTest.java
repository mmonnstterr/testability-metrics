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

import java.util.List;

import junit.framework.TestCase;

import com.google.test.metric.FieldInfo;
import com.google.test.metric.Type;
import com.google.test.metric.Variable;
import com.google.test.metric.method.op.stack.GetField;
import com.google.test.metric.method.op.stack.Invoke;
import com.google.test.metric.method.op.stack.Load;
import com.google.test.metric.method.op.stack.PutField;
import com.google.test.metric.method.op.turing.MethodInvokation;
import com.google.test.metric.method.op.turing.Operation;

public class BlockTest extends TestCase {

	public void testBlockToString() throws Exception {
		Block block = new Block("1");
		assertEquals("Block[1]{\n}", block.toString());
		
		block.addOp(new Load(-1, var(1)));
		assertEquals("Block[1]{\n  load 1{object}\n}", block.toString());
	}
	
	public void testVariableStaticAssignment() throws Exception {
		Block block = new Block("1");
		block.addOp(new Load(-1, var(1)));
		block.addOp(new PutField(-1, new FieldInfo(null, "abc", Type.ADDRESS, true, false)));
		
		List<Operation> operations = new Stack2Turing(block).translate();
		assertEquals("[null.abc{object} <- 1{object}]", operations.toString());
	}
	
	public void testVariableAssignment() throws Exception {
		Block block = new Block("1");
		block.addOp(new Load(-1, var("this"))); // this
		block.addOp(new Load(-1, var(1)));
		block.addOp(new PutField(-1, new FieldInfo(null, "abc", Type.ADDRESS, false, false)));
		
		List<Operation> operations = new Stack2Turing(block).translate();
		assertEquals("[null.abc{object} <- 1{object}]", operations.toString());
	}
	
	public void testGetField() throws Exception {
		Block block = new Block("1");
		block.addOp(new GetField(-1, new FieldInfo(null, "src", Type.ADDRESS, true, false)));
		block.addOp(new PutField(-1, new FieldInfo(null, "dst", Type.ADDRESS, true, false)));
		
		List<Operation> operations = new Stack2Turing(block).translate();
		assertEquals("[null.dst{object} <- null.src{object}]", operations.toString());
	}
	
	public void testMethodInvocation() throws Exception {
		Block block = new Block("1");
		block.addOp(new Load(-1, var("methodThis"))); // this
		block.addOp(new GetField(-1, new FieldInfo(null, "p1", Type.ADDRESS, true, false)));
		block.addOp(new GetField(-1, new FieldInfo(null, "p2", Type.ADDRESS, true, false)));
		block.addOp(new Invoke(-1, null, "methodA", "(II)I", 2, false, Type.INT));
		block.addOp(new PutField(-1, new FieldInfo(null, "dst", Type.ADDRESS, true, false)));
		
		List<Operation> operations = new Stack2Turing(block).translate();
		assertEquals("[null.methodA(II)I, null.dst{object} <- ?{object}]", operations.toString());
	}
	
	private Variable var(Object value) {
		return new Constant(value, Type.ADDRESS);
	}

	public void testDiamondBlockArrangment() throws Exception {
		Block root = new Block("root");
		Block branchA = new Block("branchA");
		Block branchB = new Block("branchB");
		Block joined = new Block("joined");
		root.addNextBlock(branchA);
		root.addNextBlock(branchB);
		branchA.addNextBlock(joined);
		branchB.addNextBlock(joined);
		
		root.addOp(new Load(-1, var("this")));
		root.addOp(new Load(-1, var("root")));
		branchA.addOp(new Load(-1, var("A")));
		branchB.addOp(new Load(-1, var("B")));
		joined.addOp(new Load(-1, var("joined")));
		joined.addOp(new Invoke(-1, null, "m", "(III)V", 3, false, null));
		
		List<Operation> operations = new Stack2Turing(root).translate();
		assertEquals(2, operations.size());
		MethodInvokation m1 = (MethodInvokation) operations.get(0);
		MethodInvokation m2 = (MethodInvokation) operations.get(1);
		
		assertEquals("[root{object}, B{object}, joined{object}]", m1.getParameters().toString());
		assertEquals("this{object}", m1.getMethodThis().toString());
		assertEquals("[root{object}, A{object}, joined{object}]", m2.getParameters().toString());
		assertEquals("this{object}", m2.getMethodThis().toString());
	}

}
