/**
 * checKing - Scorecard for software development processes
 * [C] Optimyth Software Technologies, 2009
 * Created by: lrodriguez Date: 12/10/13 10:46 PM
 */

package com.optimyth.qaking.rules.samples.java;

import com.als.clases.AbstractJavaRule;
import com.als.core.RuleContext;
import com.als.core.ast.NodePredicate;
import com.als.jkingcore.ast.ASTCatchStatement;
import com.als.jkingcore.ast.ASTCompilationUnit;
import com.als.jkingcore.ast.ASTInstanceOfExpression;

import static com.als.jkingcore.ast.SimpleNode.cast;

/**
 * InstanceofInCatchBlock - Sample rule that reports a bad practice: usage of instanceof operator
 * in catch blocks on the exception variable.
 * <p/>
 * Within a catch block the instanceof operator should not be used to distinguish between exceptions:
 * that's the purpose of the catch block itself.
 * <p/>
 * This is an example of the low-level API (TreeNode). For simple things it is very easy to
 * locate the nodes of interest in the AST, using TreeNode.
 *
 * @author <a href="mailto:lrodriguez@optimyth.org">lrodriguez</a>
 * @version 10-12-2013
 */
public class InstanceofInCatchBlock extends AbstractJavaRule {

  @Override protected void visit(ASTCompilationUnit root, RuleContext ctx) {
    root.accept(node -> {
      if(node instanceof ASTCatchStatement) {
        ASTCatchStatement catchBlock = (ASTCatchStatement)node;
        String excVar = catchBlock.getFormalParameter().getDecl().getImage();
        // Report all usages of instanceof on the exception variable, using the auxiliar predicate
        NodePredicate pred = hasInstanceOf( excVar );

        // find in catch body subtree
        catchBlock.getBody().accept(badInstanceOf -> {
          if (pred.is(badInstanceOf)) {
            report(ctx, badInstanceOf);
          }
        });
      }
    });
  }

  // Builds auxiliar predicate that matches instanceof usages on the exception variable
  private static NodePredicate hasInstanceOf(String exceptionVar) {
    return instOf -> {
      return
        instOf instanceof ASTInstanceOfExpression && // instanceof operator
        exceptionVar.equals( cast(instOf).find("Name").getImage() ); // same var referenced
    };
  }
}
