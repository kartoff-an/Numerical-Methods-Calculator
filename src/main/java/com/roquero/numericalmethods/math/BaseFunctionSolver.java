package com.roquero.numericalmethods.math;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.function.Function;

/**
 * Base class for solvers that evaluate mathematical functions.
 * Extends BaseConfigurableSolver to include rounding capabilities.
 */
public abstract class BaseFunctionSolver extends BaseConfigurableSolver {

    protected String functionStr;
    protected boolean expressionValid = false;
    protected String validationError = null;
    protected Function<Double, Double> function;

    protected BaseFunctionSolver(String functionStr, SolverConfig config) {
        super(config);
        this.functionStr = functionStr;
        validateAndBuildExpression();
    }

    protected void validateAndBuildExpression() {
        try {
            Expression testExpr = new ExpressionBuilder(functionStr)
                    .variable("x")
                    .build();

            // locality validation
            double x = 0;
            double h = 0.000001;
            double[] samples = {x - 2*h, x - h, x + h, x + 2*h};

            for (double xi : samples) {
                testExpr.setVariable("x", xi).evaluate();
            }

            this.function = createFunction(functionStr);
            this.expressionValid = true;
            this.validationError = null;

        } catch (Exception e) {
            this.expressionValid = false;
            this.validationError = e.getMessage();
        }
    }

    protected Function<Double, Double> createFunction(String expression) {
        return (Double x) -> {
            try {
                Expression expr = new ExpressionBuilder(expression)
                        .variable("x")
                        .build()
                        .setVariable("x", x);
                return expr.evaluate();
            } catch (Exception e) {
                throw new RuntimeException("Error evaluating function at x = " + x, e);
            }
        };
    }

    public double evaluateFunction(double x) {
        if (!expressionValid) {
            throw new IllegalStateException("Invalid function expression: " + validationError);
        }
        return function.apply(x);
    }

    protected void checkExpressionValidity() {
        if (!expressionValid) {
            throw new IllegalStateException("Invalid function expression: " + validationError);
        }
    }

    public String getValidationError() {
        return validationError;
    }

    public boolean isExpressionValid() {
        return expressionValid;
    }
}