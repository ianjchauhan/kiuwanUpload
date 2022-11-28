/**
 * 
 */
package com.optimyth.qaking.rules.samples.csharp;

import com.als.core.RuleContext;
import com.google.common.collect.ImmutableSet;
import com.optimyth.csharp.rules.AbstractCsharpRule;
import com.optimyth.csharp.utils.DetailAST;

import java.util.Set;

/**
 * UseOfConsoleOutput - Find uses of Console.Write or Console.WriteLine.
 *
 * We need low level AST in this kind of rules, because high level AST does not represent expressions.
 * We use CallSignature class to process method calls.
 *
 * @author <a href="mailto:jorge.para@optimyth.com">jpara</a>
 * @version 21/03/2015
 *
 */
public class UseOfConsoleOutput extends AbstractCsharpRule {
  private static final Set<String> FORBIDDEN_METHODS = ImmutableSet.of(
    "Write", "WriteLine"
  );

  @Override protected void visitAst(DetailAST ast, RuleContext ctx) {
    checkCalls(ast, ctx, call ->
      FORBIDDEN_METHODS.contains(call.getMethodName()) && (
        "Console".equals(call.getClassName()) || "System.Console".equals(call.getClassName())
      )
    );
  }

}
