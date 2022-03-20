/*
 * Copyright 2022 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nativekttypes;

import jsinterop.annotations.JsConstructor;

public class Main {
  public NativeTopLevel<String> topLevelField;
  public NativeTopLevel.Nested<String> nestedField;
  public NativeTopLevel<String>.Inner<String> innerField;

  public void methodArguments(
      NativeTopLevel<String> foo,
      NativeTopLevel.Nested<String> nested,
      NativeTopLevel<String>.Inner<String> inner) {}

  public void memberAccess() {
    NativeTopLevel<String> topLevel = new NativeTopLevel<>("foo");
    String fooInstanceMethod = topLevel.instanceMethod("foo");
    String fooStaticMethod = topLevel.staticMethod("foo");
    String fooInstanceField = topLevel.instanceField;
    topLevel.instanceField = "foo";
    Object fooStaticField = topLevel.staticField;
    topLevel.staticField = "foo";
    int i1 = topLevel.fieldToRename;
    int i2 = topLevel.methodToRename();
    int i3 = topLevel.getMethodAsProperty();
    int i4 = topLevel.nonGetMethodAsProperty();
    int i5 = topLevel.methodToRenameAsProperty();
    boolean i6 = topLevel.isFieldToRename;
    boolean i7 = topLevel.isMethodAsProperty();
    int i8 = topLevel.getstartingmethodAsProperty();

    NativeTopLevel.Nested<String> nested = new NativeTopLevel.Nested<>("foo");
    String nestedInstanceMethod = nested.instanceMethod("foo");
    String nestedStaticMethod = nested.staticMethod("foo");
    String nestedInstanceField = nested.instanceField;
    nested.instanceField = "foo";
    Object nestedStaticField = nested.staticField;
    nested.staticField = "foo";

    NativeTopLevel<String>.Inner<String> inner = topLevel.new Inner<String>("foo");

    Subclass<String> subclass = new Subclass<>("foo");
    int i9 = subclass.methodToRename();
    int i10 = subclass.interfaceMethod();
    int i11 = subclass.interfaceMethodToRename();
  }

  public void typeLiterals() {
    Class<?> c1 = NativeTopLevel.class;
    Class<?> c2 = NativeTopLevel.Nested.class;
    Class<?> c3 = NativeTopLevel.Inner.class;
  }

  public void casts() {
    NativeTopLevel<String> o1 = (NativeTopLevel<String>) null;
    NativeTopLevel.Nested<String> o2 = (NativeTopLevel.Nested<String>) null;
    NativeTopLevel<String>.Inner<String> o3 = (NativeTopLevel<String>.Inner<String>) null;
  }
}

class Subclass<V> extends NativeTopLevel<V> implements NativeInterface<V> {
  @JsConstructor
  Subclass(V v) {
    super(v);
  }

  @Override
  public int methodToRename() {
    return super.methodToRename();
  }

  @Override
  public int interfaceMethod() {
    return 0;
  }

  @Override
  public int interfaceMethodToRename() {
    return 0;
  }
}
