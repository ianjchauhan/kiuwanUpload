/**
 * 
 */
package com.optimyth.qaking.rules.samples.csharp;

import com.als.core.RuleContext;
import com.optimyth.csharp.hla.model.CsharpCompilationUnit;
import com.optimyth.csharp.hla.model.CsharpMethod;
import com.optimyth.csharp.hla.model.CsharpType;
import com.optimyth.csharp.rules.AbstractCsharpRule;

/**
 * VirtualMembersInSealedTypes - Find not sealed virtual method declarations in sealed types
 * using high level AST. High level AST is a more compact, simple representation of AST, useful
 * depending on what we need to check in a rule.
 * 
 * @author <a href="mailto:jorge.para@optimyth.com">jpara</a>
 * @version 21/03/2015
 *
 */
public class VirtualMembersInSealedTypes extends AbstractCsharpRule {

  @Override protected void visitHla(CsharpCompilationUnit cu, RuleContext ctx) {
    cu.accept(CsharpType.class, clazz -> {
      // consider only sealed classes
      if (clazz.isClass() && clazz.isSealed()) {
        // Emit violation on virtual && non-sealed methods
        for (CsharpMethod method : clazz.getRealMethods()) {
          if(method.isVirtual() && !method.isSealed()) {
            reportViolation(ctx, method);
          }
        }
      }
    });
  }
}
