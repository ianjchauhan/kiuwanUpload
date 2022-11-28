package com.optimyth.qaking.rules.samples.java;

import com.als.core.AbstractRule;
import com.als.core.RuleContext;
import com.als.core.RuleViolation;

/**
 * DefectsGenerator - Generates N defects
 *
 * @author lrodriguez
 * @version 08-Oct-2020 (lrodriguez)
 */
public class DefectsGenerator extends AbstractRule {
  // limit 100k, to avoid too much load on database if the rule is poorly configured
  private static final int MAX_DEFECTS = 100000;
  private int numDefects;

  @Override public boolean isInformative() { return true; }

  @Override public void initialize(RuleContext ctx) {
    super.initialize(ctx);
    numDefects = getProperty("numDefects", 0);
    if(numDefects > MAX_DEFECTS) numDefects = MAX_DEFECTS;
  }

  @Override public void postProcess(RuleContext ctx) {
    super.postProcess(ctx);
    for (int i = 0; i < numDefects; i++) {
      RuleViolation rv = createRuleViolation(ctx, i+1, null, Integer.toString(i+1));
      ctx.addRuleViolation(rv);
    }
  }
}
