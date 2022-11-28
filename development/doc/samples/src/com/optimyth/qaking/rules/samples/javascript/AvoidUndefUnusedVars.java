/**
 * checKing - Scorecard for software development processes
 * [C] Optimyth Software Technologies, 2009
 * Created by: lrodriguez Date: 1/20/14 8:16 PM
 */

package com.optimyth.qaking.rules.samples.javascript;

import com.als.core.RuleContext;
import com.als.core.ast.NodePredicates;
import com.als.js.rules.AbstractJavaScriptRule;
import com.optimyth.qaking.js.ast.JSNode;
import com.optimyth.qaking.js.ast.JSRoot;
import com.optimyth.qaking.js.symbols.LocalSymbolTable;
import com.optimyth.qaking.js.symbols.SymbolEntry;
import es.als.util.StringUtils;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.CatchClause;
import org.mozilla.javascript.ast.Scope;
import org.mozilla.javascript.ast.Symbol;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * AvoidUndefUnusedVars - Avoid undef and unused vars.
 * <p/>
 * Sample rule to show how to use (local) symbol table for resolving declarations and usages of symbols
 * (functions, function parameters, variables, constants...)
 * <p/>
 * The rule shows also how to fetch a configuration property from a comment in source code,
 * to reduce the number of violations reported.
 * <p/>
 * You may feel free to change or adapt the rule as appropiate:
 * check unused inner functions (functions defined inside other functions but not called),
 * check undef symbols when there is a symbol named the same but with different case,
 * etc.
 *
 * @author <a href="mailto:lrodriguez@optimyth.org">lrodriguez</a>
 * @version 20-01-2014
 */
public class AvoidUndefUnusedVars extends AbstractJavaScriptRule {

  private boolean checkUnusedException = true;
  private boolean checkUnusedParameter = true;
  private boolean checkUnusedGlobal = true;
  private Set<String> knownGlobals;

  // Filter variables to check for no usages
  // Restrict to variable and parameter declarations, excluding exception names: catch(e) {} is allowed
  private final Predicate<Symbol> UNUSED = symbol -> {
    AstNode target = (AstNode)symbol.getNode();
    if(!checkUnusedException && target != null && target.getParent() instanceof CatchClause) {
      return false;
    }

    if(checkUnusedParameter && symbol.isParameter()) return true;
    if(symbol.isVariable() || symbol.isConstant()) {
      // check if globals should be ignored
      Scope scope = symbol.getContainingTable();
      return checkUnusedGlobal || scope == null || scope.getParentScope() != null;
    }
    return symbol.isLet();
  };

  // Check only vars apparently global (no declaration in same source unit),
  // excluding known symbols (built-in or "standard" symbols) to avoid too many positives
  private final Predicate<SymbolEntry> UNDEF = se -> {
    if(knownGlobals.contains(se.getName())) return false; // known global
    // ignore Class references, to avoid FPs
    if(Character.isUpperCase( se.getName().charAt(0) )) return false;
    // unused
    return UNUSED.test(se.getSymbol()) && !se.isKnownGlobalSymbol();
  };

  /** Configure rule from its properties */
  @Override public void initialize(RuleContext ctx) {
    super.initialize(ctx);
    this.checkUnusedException = getProperty("checkUnusedException", true);
    this.checkUnusedParameter = getProperty("checkUnusedParameter", true);
    this.checkUnusedGlobal = getProperty("checkUnusedGlobal", true);
    this.knownGlobals = getPropertySet("knownGlobals", "");
  }

  @Override protected void visit(JSRoot root, RuleContext ctx) {
    // Build symbol table for this source unit
    LocalSymbolTable symtab = LocalSymbolTable.build(root);

    // Fetch configured globals in source code comment
    Set<String> configuredGlobals = getGlobals(root);

    // Report unused vars
    List<SymbolEntry> unusedList = symtab.getUnusedSymbols(UNUSED, NodePredicates.isTrue());
    for(SymbolEntry unused : unusedList) {
      String msg = "Unused symbol " + unused.getSymbol().getName();
      reportViolation(ctx, unused.getDefinition(), msg);
    }

    // Report undefined vars used
    List<SymbolEntry> undefList = symtab.getGlobalSymbols(UNDEF);
    for(SymbolEntry undef : undefList) {
      if(configuredGlobals.contains( undef.getName() )) continue; // ignore a global registered in source comment
      String msg = "Undefined symbol: " + undef.getSymbol().getName();

      for(JSNode undefUsage : undef.getUsages()) {
        reportViolation(ctx, undefUsage, msg);
      }
    }
  }

  /** Fetch configuration in source comments: <code>QAK globals: comma-separated list of globals</code> */
  private Set<String> getGlobals(JSRoot root) {
    String config = getConfigurationDirective(root.get(), "globals");
    return config != null ? StringUtils.asSet(config, ',') : Collections.emptySet();
  }

}
