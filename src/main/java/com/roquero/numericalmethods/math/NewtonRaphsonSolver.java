package com.roquero.numericalmethods.math;

import java.util.ArrayList;
import java.util.List;

public class NewtonRaphsonSolver extends BaseIterativeSolver {

    private final double x0;

    public record Iteration(
            int iteration,
            double xn,
            double fx,
            double dfx,
            double xn1,
            double error
    ) {}

    public record Result(
            double root,
            List<Iteration> iterations,
            double convergenceTest,
            boolean converged
    ) {}

    public NewtonRaphsonSolver(String functionStr, double x0, SolverConfig config) {
        super(functionStr, config);
        this.x0 = x0;
    }

    private double firstDerivative(double x) {
        double h = Math.pow(10, -(getDecimalPlaces() + 1));
        double fxPlusH = evaluateFunction(x + h);
        double fxMinusH = evaluateFunction(x - h);
        return (fxPlusH - fxMinusH) / (2 * h);
    }

    private double secondDerivative(double x) {
        double h = Math.pow(10, -(getDecimalPlaces() + 1));
        double fxPlusH = evaluateFunction(x + h);
        double fx = evaluateFunction(x);
        double fxMinusH = evaluateFunction(x - h);
        return (fxPlusH - 2 * fx + fxMinusH) / (h * h);
    }

    @Override
    public Result solve() {
        checkExpressionValidity();

        double x = maybeRound(x0);
        List<Iteration> iterations = new ArrayList<>();

        for (int i = 1; i <= maxIterations; i++) {
            double fx;
            double dfx;

            try {
                fx = maybeRound(evaluateFunction(x));
                dfx = maybeRound(firstDerivative(x));
            } catch (Exception e) {
                return new Result(x, iterations, getConvergenceTest(), false);
            }

            if (Math.abs(dfx) < 1e-12) {
                return new Result(x, iterations, getConvergenceTest(), false);
            }

            double nextX = maybeRound(x - fx / dfx);
            double error = maybeRound(Math.abs(nextX - x));

            iterations.add(new Iteration(i, x, fx, dfx, nextX, error));

            if (error <= tolerance) {
                return new Result(nextX, iterations, getConvergenceTest(), true);
            }

            x = nextX;
        }

        return new Result(x, iterations, getConvergenceTest(), false);
    }

    public double getConvergenceTest() {
        double functionValue = maybeRound(evaluateFunction(x0));
        double firstDerivative = maybeRound(firstDerivative(x0));
        double secondDerivative = maybeRound(secondDerivative(x0));

        return maybeRound(
                Math.abs(
                        (functionValue * secondDerivative) / (firstDerivative * firstDerivative)
                )
        );
    }
}