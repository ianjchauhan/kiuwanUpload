/**
 * checKing - Scorecard for software development processes
 * [C] Optimyth Software Technologies, 2009
 * Created by: lrodriguez Date: 2/18/14 1:52 PM
 */

package com.optimyth.qaking.rules.samples.cpp;

import com.als.core.RuleContext;
import com.als.core.ast.BaseNode;
import com.kiuwan.qaking.cpp.ast.FunctionDefinition;
import com.kiuwan.qaking.cpp.ast.ReturnStatement;
import com.kiuwan.qaking.cpp.ast.Statement;
import com.optimyth.cpp.rules.AbstractCppRule;

import java.util.List;

/**
 * SingleReturnAtEnd - Sample rule that checks if function definitions have at most one return statement as last
 * statement in function body. This is required by IEC 61508 (or MISRA-C 14.7), under good programming style,
 * to avoid subtle errors in control logic.
 *
 * @author <a href="mailto:lrodriguez@optimyth.org">lrodriguez</a>
 * @version 18-02-2014
 */
public class SingleReturnAtEnd extends AbstractCppRule {
  
  @Override protected void doVisit(BaseNode function, RuleContext ctx) {
    if(function instanceof FunctionDefinition) {
      FunctionDefinition f = (FunctionDefinition)function;

      List<ReturnStatement> rets = f.findAll(ReturnStatement.class);
      if(rets.isEmpty()) return; // no ret, allowed

      if(rets.size() > 1) {
        // 2 or more returns, violation
        ctx.getReport().addRuleViolation( violation(ctx, function) );

      } else {
        // violation unless return is the last statement in function body
        ReturnStatement expected = rets.get(0);
        Statement last = f.body().lastStatement();
        // Statement wraps the operation
        if(last.content() != expected) {
          // Return was NOT the last statement
          ctx.addRuleViolation( violation(ctx, function) );
        }
      }
    }
  }
  
}
