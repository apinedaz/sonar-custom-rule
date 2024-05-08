/*
 * Copyright (C) 2012-2022 SonarSource SA - mailto:info AT sonarsource DOT com
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
package org.sonar.samples.java.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.ParameterizedTypeTree;
import org.sonar.plugins.java.api.tree.Tree.Kind;
import org.sonar.plugins.java.api.tree.TypeTree;


@Rule(key = "NotObject")
public class NotObjectRule extends BaseTreeVisitor implements JavaFileScanner {

  private JavaFileScannerContext context;
  @Override
  public void scanFile(JavaFileScannerContext context) {
    this.context = context;
    scan(context.getTree());
  }

  @Override
  public void visitMethod(MethodTree tree) {
    super.visitMethod(tree);
    TypeTree returnType = tree.returnType();
    if (returnType != null && returnType.is(Kind.PARAMETERIZED_TYPE)) {
      ParameterizedTypeTree parameterizedType = (ParameterizedTypeTree) returnType;
      if (parameterizedType.symbolType().is("org.springframework.http.ResponseEntity") &&
        parameterizedType.typeArguments().size() == 1&&
        parameterizedType.typeArguments().get(0).symbolType().is("java.lang.Object")) {
        /*for (int i = 0; i < parameterizedType.typeArguments().size(); i++) {
          //if (typeA.symbolType().is("java.lang.Object")
        }*/
          context.reportIssue(this,tree.simpleName(),(((ParameterizedTypeTree) parameterizedType.typeArguments().get(0)).symbolType())+"");
      }
    }

  }
}
