package org.serverless.workflow.impl.expression;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.serverless.workflow.api.ExpressionEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JexlExpressionEvaluatorImpl implements ExpressionEvaluator {

    public JexlEngine jexl = new JexlBuilder().create();

    private static Logger logger = LoggerFactory.getLogger(JexlExpressionEvaluatorImpl.class);

    @Override
    public String getName() {
        return "jexl";
    }

    @Override
    public boolean evaluate(String expression,
                            String triggerName) {
        try {
            JexlExpression e = jexl.createExpression(expression);

            JexlContext jc = new MapContext();
            jc.set("trigger",
                   triggerName);

            return (Boolean) e.evaluate(jc);
        } catch (Exception e) {
            logger.error("Unable to evaluate expression: " + expression + " with error: " + e.getMessage());
            return false;
        }
    }
}
