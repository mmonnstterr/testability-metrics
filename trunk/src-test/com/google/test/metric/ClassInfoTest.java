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

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class ClassInfoTest extends TestCase {

	ClassRepository repo = new ClassRepository();

	public void testNonExistingClass() throws Exception {
		try {
			repo.getClass("IDontExistClass");
			fail();
		} catch (ClassNotFoundException e) {
			assertTrue(e.getMessage().contains("IDontExistClass"));
			assertEquals("IDontExistClass", e.getClassName());
		}
	}

	public static class EmptyClass {
	}

	public void testParseEmptyClass() throws Exception {
		ClassInfo clazz = repo.getClass(EmptyClass.class);
		assertEquals(EmptyClass.class.getName(), clazz.getName());
		assertEquals(EmptyClass.class.getName(), clazz.toString());
		assertSame(clazz, repo.getClass(EmptyClass.class));
	}

	public void testMethodNotFoundException() throws Exception {
		ClassInfo clazz = repo.getClass(EmptyClass.class);
		try {
			clazz.getMethod("IDontExistMethod()V");
			fail();
		} catch (MethodNotFoundException e) {
			assertTrue(e.getMessage().contains("IDontExistMethod()V"));
			assertTrue(e.getMessage().contains(EmptyClass.class.getName()));
			assertEquals("IDontExistMethod()V", e.getMethodName());
			assertEquals(clazz, e.getClassInfo());
		}
	}

	public static class SingleMethodClass {
		public void methodA() {
		}
	}

	public void testParseSingleMethodClass() throws Exception {
		ClassInfo clazz = repo.getClass(SingleMethodClass.class);
		MethodInfo method = clazz.getMethod("methodA()V");
		assertEquals("methodA()V", method.getNameDesc());
		assertEquals(SingleMethodClass.class.getName() + ".methodA()V", method
				.toString());
		assertSame(method, clazz.getMethod("methodA()V"));
	}

	public void testFiledNotFound() throws Exception {
		ClassInfo clazz = repo.getClass(EmptyClass.class);
		try {
			clazz.getField("IDontExistField");
			fail();
		} catch (FieldNotFoundException e) {
			assertTrue(e.getMessage().contains("IDontExistField"));
			assertTrue(e.getMessage().contains(EmptyClass.class.getName()));
			assertEquals("IDontExistField", e.getFieldName());
			assertEquals(clazz, e.getClassInfo());
		}
	}

	public static class SingleFieldClass {
		Object fieldA;
	}

	public void testParseFields() throws Exception {
		ClassInfo clazz = repo.getClass(SingleFieldClass.class);
		FieldInfo field = clazz.getField("fieldA");
		assertEquals("fieldA", field.getName());
		assertEquals(SingleFieldClass.class.getName() + ".fieldA{object}", field
				.toString());
		assertSame(field, clazz.getField("fieldA"));
	}

	public static class LocalVarsClass {
		public void method() {
		}

		public static void staticMethod() {
		}

		public void method3(Object a, int b, int[] c) {
			Object d = null;
			a = d;
		}
		public static void staticMethod3(Object a, int b, int[] c) {
			Object d = null;
			a = d;
		}
	}

	public void testLocalVarsMethod() throws Exception {
		assertLocalVars("method()V", params(), locals("this"));
	}

	public void testLocalVarsStaticMethod() throws Exception {
		assertLocalVars("staticMethod()V", params(), locals());
	}

	public void testLocalVarsMethod3() throws Exception {
		assertLocalVars("method3(Ljava/lang/Object;I[I)V",
				params("a", "b", "c"), locals("this", "d"));
	}

	public void testLocalVarsStaticMethod3() throws Exception {
		assertLocalVars("staticMethod3(Ljava/lang/Object;I[I)V",
				params("a", "b", "c"), locals("d"));
	}

	private void assertLocalVars(String method, String[] params, String[] locals) {
		ClassInfo classInfo = repo.getClass(LocalVarsClass.class);
		MethodInfo methodInfo = classInfo.getMethod(method);
		List<ParameterInfo> paramsParse = methodInfo.getParameters();
		List<LocalVariableInfo> localsParse = methodInfo.getLocalVariables();
		assertEquals("Expecting " + Arrays.toString(params) + " found "
				+ paramsParse, params.length, paramsParse.size());
		assertEquals("Expecting " + Arrays.toString(locals) + " found "
				+ localsParse, locals.length, localsParse.size());
		for (int i = 0; i < params.length; i++) {
			assertEquals(params[i], paramsParse.get(i).getName());
		}
		for (int i = 0; i < locals.length; i++) {
			assertEquals(locals[i], localsParse.get(i).getName());
		}
	}

	private String[] params(String... strings) {
		return strings;
	}

	private String[] locals(String... strings) {
		return strings;
	}
	

	public void testJavaLangObject() throws Exception {
		repo.getClass(Object.class);
	}
	
	public void testJavaLangString() throws Exception {
		repo.getClass(String.class);
	}
	
	private static class Monitor {
		public void  method() {
			synchronized (this) {
				hashCode();
			}
		}
		public void  method2() {
			hashCode();
			synchronized (this) {
				hashCode();
			}
			hashCode();
		}
	}
	
	public void testMonitor() throws Exception {
		repo.getClass(Monitor.class);
	}
}
