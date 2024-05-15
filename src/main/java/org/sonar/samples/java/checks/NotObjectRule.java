/*
 * Copyright (C) 2012-2022 SonarSource SA - mailto:info AT sonarsource DOT com
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
package org.sonar.samples.java.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.semantic.Type;
import org.sonar.plugins.java.api.tree.*;
import org.sonar.plugins.java.api.tree.Tree.Kind;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;

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
    // Check if the method returns ResponseEntity<Object>
    TypeTree returnType = tree.returnType();
    if (returnType != null && returnType.is(Kind.PARAMETERIZED_TYPE)) {
      ParameterizedTypeTree paramType = (ParameterizedTypeTree) returnType;
      if (isResponseEntityObject(paramType)) {
        reportIssue(returnType, "Avoid using ResponseEntity<Object> as return type.");
      }
    }
  }

  private boolean isResponseEntityObject(ParameterizedTypeTree paramType) {
    // Check if the type is ResponseEntity<Object>
    if (paramType.type().is(Kind.IDENTIFIER)) {
      IdentifierTree identifier = (IdentifierTree) paramType.type();
      if ("ResponseEntity".equals(identifier.name())) {
        if (paramType.typeArguments().size() == 1 && isObjectType(paramType.typeArguments().get(0))) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isObjectType(Tree tree) {
    if (tree.is(Kind.IDENTIFIER)) {
      Symbol symbol = ((IdentifierTree) tree).symbol();
      if (symbol != null) {
        Type type = symbol.type();
        return type.is("java.lang.Object");
      }
    }
    return false;
  }

  private void reportIssue(Tree tree, String message) {
    context.reportIssue(this, tree, message);
  }
}
