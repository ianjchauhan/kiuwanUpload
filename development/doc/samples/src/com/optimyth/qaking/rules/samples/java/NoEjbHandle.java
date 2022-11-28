/**
 * qaking
 * [C] Optimyth Software, 2016
 * Created by: lrodriguez Date: 29/11/2016 23:27
 */

package com.optimyth.qaking.rules.samples.java;

import com.als.clases.AbstractJavaRule;
import com.als.core.RuleContext;
import com.als.core.ast.BaseNode;
import com.als.core.ast.NodePredicate;
import com.als.jkingcore.ast.ASTClassOrInterfaceType;
import com.als.jkingcore.ast.ASTCompilationUnit;
import com.als.jkingcore.ast.ASTImportDeclaration;
import com.optimyth.qaking.highlevelapi.dsl.Query;

import static com.als.core.ast.NodePredicates.or;

/**
 * NoEjbHandle - Detects usages for javax.ejb.Handle, both in imports and in type references.
 *
 * @author <a href="mailto:lrodriguez@optimyth.org">lrodriguez</a>
 * @version 29-11-2016
 */
public class NoEjbHandle extends AbstractJavaRule {
  private static final String FORBIDDEN_TYPE = "javax.ejb.Handle";

  private final NodePredicate typePred = new NodePredicate() {
    @Override public boolean is(BaseNode node) {
      if(node instanceof ASTClassOrInterfaceType) {
        String type = ((ASTClassOrInterfaceType)node).getName();
        return FORBIDDEN_TYPE.equals(type);
      }
      return false;
    }
  };

  private final NodePredicate importPred = new NodePredicate() {
    @Override public boolean is(BaseNode node) {
      if(node instanceof ASTImportDeclaration) {
        ASTImportDeclaration imp = (ASTImportDeclaration)node;
        return
          FORBIDDEN_TYPE.equals(imp.getImportedName()) ||
          (imp.isStatic() && FORBIDDEN_TYPE.equals(imp.getPackageName()));
      }
      return false;
    }
  };

  private final Query query = Query.query().find(or(importPred, typePred)).report();

  @Override protected void visit(ASTCompilationUnit cu, RuleContext ctx) {
    query.run(this, ctx, cu);
  }

}
