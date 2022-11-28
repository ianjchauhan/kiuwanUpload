/**
 * 
 */
package com.optimyth.qaking.rules.samples.csharp;

import com.als.core.RuleContext;
import com.als.core.ast.BaseNode;
import com.optimyth.csharp.rules.AbstractCsharpRule;
import com.optimyth.csharp.symboltable.LocalSymbolTable;
import com.optimyth.csharp.symboltable.Symbol;
import com.optimyth.csharp.symboltable.SymbolKind;

import java.util.function.Predicate;

import static com.optimyth.csharp.symboltable.LocalSymbolTableBuilder.getSymbolTable;
import static com.optimyth.csharp.utils.DetailAST.cast;

/**
 * UnusedLocalVar - Find unused local variables, using local symbol table.
 * 
 * @author <a href="mailto:jorge.para@optimyth.com">jpara</a>
 * @version 21/03/2015
 */
public class UnusedLocalVar extends AbstractCsharpRule {

  // Predicate that matches variable symbols with no usages. It can be easily adapted to match fields or methods.
  private static final Predicate<Symbol> localVarNotUsed = symbol ->
    symbol.getKind() == SymbolKind.VARIABLE && !symbol.hasUsages();
  
  @Override protected void visit(BaseNode root, RuleContext ctx) {
    LocalSymbolTable table = getSymbolTable(cast(root));
    //Just find symbols matching predicate and report violations
    for (Symbol notUsed : table.findAll(localVarNotUsed)) {
      reportViolation(ctx, notUsed.getNode());
    }
  }
  
}
