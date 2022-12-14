/**
 * checKing - Scorecard for software development processes
 * [C] Optimyth Software Technologies, 2009
 * Created by: lrodriguez Date: 2/18/14 1:36 PM
 */

package com.optimyth.qaking.rules.samples.cpp;

import com.als.core.RuleContext;
import com.als.core.ast.BaseNode;
import com.optimyth.cpp.rules.AbstractCppRule;

import java.util.Set;

import static com.optimyth.qaking.cpp.hla.primitives.CppPredicates.C_FUNCTION_CALL;
import static com.optimyth.qaking.cpp.util.FunctionUtil.getCFunctionCallSignature;

/**
 * AvoidSignalManagementFunctions - Sample rule to check for calls of C signal management functions.
 *
 * @author <a href="mailto:lrodriguez@optimyth.org">lrodriguez</a>
 * @version 18-02-2014
 */
public class AvoidSignalManagementFunctions extends AbstractCppRule {

  private static final String DEFAULT_SIGNAL_FUNCTIONS = "kill, raise, signal, sighold, sigrelse, sigignore, sigpause";
  private Set<String> signalFunctions;

  public void initialize(RuleContext ctx){
    super.initialize(ctx);
    signalFunctions = getPropertySet("signalFunctions", DEFAULT_SIGNAL_FUNCTIONS);
  }

  // For each function call, fetch the function name and emit violation when a signal handling function
  @Override protected void doVisit(BaseNode call, RuleContext ctx) {
    if(C_FUNCTION_CALL.is(call)) {
      String funcionName = getCFunctionCallSignature(call);
      if (signalFunctions.contains(funcionName)) {
        ctx.addRuleViolation( violation(ctx, call) );
      }
    }
  }

}
