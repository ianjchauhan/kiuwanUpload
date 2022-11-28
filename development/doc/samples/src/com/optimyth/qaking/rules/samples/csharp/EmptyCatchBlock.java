/**
 * 
 */
package com.optimyth.qaking.rules.samples.csharp;

import com.als.core.RuleContext;
import com.optimyth.csharp.hla.model.CsharpCompilationUnit;
import com.optimyth.csharp.rules.AbstractCsharpRule;
import com.optimyth.qaking.highlevelapi.ast.exception.HLACatch;
import com.optimyth.qaking.highlevelapi.ast.statement.HLABlock;

/**
 * EmptyCatchBlock - Find empty catch blocks using high level AST.
 * High level AST is a more compact, simple representation of AST, useful
 * depending on what we need to check in a rule.
 * 
 * @author <a href="mailto:jorge.para@optimyth.com">jpara</a>
 * @version 21/03/2015
 *
 */
public class EmptyCatchBlock extends AbstractCsharpRule {

  @Override protected void visitHla(CsharpCompilationUnit cu, RuleContext ctx) {
    cu.accept(HLACatch.class, _catch -> checkCatchBlock(ctx, _catch));
  }

  private void checkCatchBlock(RuleContext ctx, HLACatch catchBlock) {
    if (catchBlock.child(HLABlock.class).isLeaf()) {
      reportViolation(ctx, catchBlock);
    }
  }
  
}
