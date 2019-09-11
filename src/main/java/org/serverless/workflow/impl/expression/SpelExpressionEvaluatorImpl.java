package org.serverless.workflow.impl.expression;

import org.serverless.workflow.api.ExpressionEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class SpelExpressionEvaluatorImpl implements ExpressionEvaluator {

    private static Logger logger = LoggerFactory.getLogger(SpelExpressionEvaluatorImpl.class);

    @Override
    public String getName() {
        return "spel";
    }

    @Override
    public boolean evaluate(String expression,
                            String triggerName) {
        try {
            ExpressionParser spelExpressionParser = new SpelExpressionParser();
            Expression spelExpression = spelExpressionParser.parseExpression(expression);

            EvaluationContext context = new StandardEvaluationContext(new SpelRootObject(triggerName));

            return (Boolean) spelExpression.getValue(context);
        } catch (Exception e) {
            logger.error("Unable to evaluate expression: " + expression + " with error: " + e.getMessage());
            return false;
        }
    }

    private class SpelRootObject {

        private String trigger;

        public SpelRootObject(String trigger) {
            this.trigger = trigger;
        }

        public String getTrigger() {
            return trigger;
        }

        public void setTrigger(String trigger) {
            this.trigger = trigger;
        }
    }
}
