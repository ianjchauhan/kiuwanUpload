/**
 * checKing - Scorecard for software development processes
 * [C] Optimyth Software Technologies, 2009
 * Created by: lrodriguez Date: 1/20/14 5:57 PM
 */

package com.optimyth.qaking.rules.samples.javascript;

import com.als.core.RuleContext;
import com.als.core.ast.BaseNode;
import com.als.core.ast.NodePredicate;
import com.als.js.rules.AbstractJavaScriptRule;
import com.optimyth.qaking.js.ast.JSAssignment;
import com.optimyth.qaking.js.ast.JSName;
import com.optimyth.qaking.js.ast.JSNode;
import com.optimyth.qaking.js.ast.JSPropertyGet;
import es.als.util.StringUtils;

import java.util.Collections;
import java.util.Set;

/**
 * NativeObjectPrototypeOverwrite - Prohibits overwriting prototypes of native objects such as Array,
 * Date, Object, Function and so on.
 * <p/>
 * There are many ways to code prototype overwriting. Rule must first define which types are
 * considered native, and then found a PropertyGet (native object + '.prototype') at the
 * left-hand side of an assignment.
 *
 * @author <a href="mailto:lrodriguez@optimyth.org">lrodriguez</a>
 * @version 20-01-2014
 */
public class NativeObjectPrototypeOverwrite extends AbstractJavaScriptRule {

  private static final String DEFAULT_TYPES_TO_CHECK = "Object,Function,Array,String,Boolean,Number,Date,RegExp,Error," +
                                                       "window,Symbol,EvalError,InternalError,RangeError,ReferenceError," +
                                                       "SyntaxError,TypeError,URIError,Math,Int8Array,Uint8Array,Uint8," +
                                                       "ClampedArray,Int16Array,Unit16Array,Int32Array,Uint32Array," +
                                                       "Float32Array,Float64Array,Map,Set,WeakMap,WeakSet,ArrayBuffer," +
                                                       "DataView,JSON,Promise,Reflect,Proxy,Intl,Generator,Iterator," +
                                                       "ParallelArray,StopIteration,eval,arguments";

  private Set<String> typesToCheck = Collections.emptySet();

  // look for FORBIDDEN_TYPE.prototype = ... or FORBIDDEN_TYPE.prototype.newprop = ...
  private final NodePredicate PROTOTYPE_ASSIGN = new NodePredicate() {
    public boolean is(BaseNode node) {
      if(node instanceof JSPropertyGet) {
        JSPropertyGet pg = (JSPropertyGet)node;
        JSNode left = pg.getTarget();
        String propname = pg.getPropertyName();
        if(left instanceof JSName && "prototype".equals(propname)) {
          String objType = ((JSName)left).getIdentifier();
          if(typesToCheck.contains(objType)) {
            if(node.getParent() instanceof JSPropertyGet) node = node.getParent();
            return inLeftOfAssignment((JSNode)node);
          }
        }
      }
      return false;
    }

    // Check if in LHS of an assignment
    private boolean inLeftOfAssignment(JSNode propGet) {
      return propGet.getParent() instanceof JSAssignment && propGet.getChildPos() == 0;
    }
  };

  @Override public void initialize(RuleContext ctx) {
    super.initialize(ctx);
    typesToCheck = StringUtils.asSet( getProperty("typesToCheck", DEFAULT_TYPES_TO_CHECK), ',' );
  }

  // With the proper predicate, rule is trivial
  @Override protected void visit(BaseNode root, RuleContext ctx) {
    check(root, PROTOTYPE_ASSIGN, ctx);
  }
}
