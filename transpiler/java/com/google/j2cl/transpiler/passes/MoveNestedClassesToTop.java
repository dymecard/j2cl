/*
 * Copyright 2021 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.j2cl.transpiler.passes;

import com.google.j2cl.transpiler.ast.AbstractRewriter;
import com.google.j2cl.transpiler.ast.CompilationUnit;
import com.google.j2cl.transpiler.ast.LocalClassDeclarationStatement;
import com.google.j2cl.transpiler.ast.Node;
import com.google.j2cl.transpiler.ast.Statement;
import com.google.j2cl.transpiler.ast.Type;

/** Promotes nested inner classes to top scope. */
public class MoveNestedClassesToTop extends NormalizationPass {
  @Override
  public void applyTo(CompilationUnit compilationUnit) {

    compilationUnit.accept(
        new AbstractRewriter() {
          @Override
          public Node rewriteLocalClassDeclarationStatement(
              LocalClassDeclarationStatement typeDeclarationStatement) {
            getCurrentType().addType(typeDeclarationStatement.getLocalClass());
            return Statement.createNoopStatement();
          }
        });

    compilationUnit.accept(
        new AbstractRewriter() {
          @Override
          public Node rewriteType(Type type) {
            if (getParent() instanceof CompilationUnit) {
              return type;
            }
            compilationUnit.addType(type);
            return null;
          }
        });

    compilationUnit.setFlattened();
  }
}
