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

import com.google.classpath.ClasspathRootGroup;
import com.google.test.metric.asm.ClassInfoBuilderVisitor;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ClassRepository {

  private final Map<String, ClassInfo> classes = new HashMap<String, ClassInfo>();
  private ClasspathRootGroup classpathRoots;

  public ClassRepository() {
  }

  public ClassRepository(ClasspathRootGroup classpathRoots) {
    this.classpathRoots = classpathRoots;
  }

  public ClassInfo getClass(Class<?> clazz) {
    return getClass(clazz.getName());
  }

  public ClassInfo getClass(String clazzName) {
    if (clazzName.startsWith("[")) {
      return getClass(Object.class);
    }
    ClassInfo classInfo = classes.get(clazzName.replace('/', '.'));
    if (classInfo == null) {
        classInfo = parseClass(inputStreamForClass(clazzName));
    }
    return classInfo;
  }

  private InputStream inputStreamForClass(String clazzName) {
    String classResource = clazzName.replace(".", "/") + ".class";
    InputStream classBytes;
    if (classpathRoots != null) {
      classBytes = classpathRoots.getResourceAsStream(classResource);
    } else {
      classBytes = ClassLoader.getSystemResourceAsStream(classResource);
    }
    if (classBytes == null) {
      throw new ClassNotFoundException(clazzName);
    }
    return classBytes;
  }

  private ClassInfo parseClass(InputStream classBytes) {
    try {
      ClassReader classReader = new ClassReader(classBytes);
      ClassInfoBuilderVisitor visitor = new ClassInfoBuilderVisitor(this);
      classReader.accept(visitor, 0);
      return visitor.getClassInfo();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void addClass(ClassInfo classInfo) {
    classes.put(classInfo.getName(), classInfo);
  }

}

