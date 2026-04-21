package com.roquero.numericalmethods.math;

import java.util.ArrayList;
import java.util.List;

public class CentralDividedDifferenceSolver extends BaseFunctionSolver {

    private double x;
    private final double h;

    public record DetailedDerivativeResult(
            double x,
            double h,
            double xn_minus_1,
            double xn,
            double xn_plus_1,
            double f_xn_minus_1,
            double f_xn,
            double f_xn_plus_1,
            double firstDerivative,
            double secondDerivative,
            double truncationError,
            String firstDerivativeFormula,
            String secondDerivativeFormula,
            String firstDerivativeCalculation,
            String secondDerivativeCalculation
    ) {}

    public record RichardsonResult(
            int order,
            double h,
            double derivative,
            double error
    ) {}

    public record RichardsonStep(
            String description,
            String formula,
            String calculation,
            double result
    ) {}

    public record RichardsonExtrapolationDetails(
            List<RichardsonResult> results,
            List<RichardsonStep> firstDerivativeSteps,
            List<RichardsonStep> secondDerivativeSteps
    ) {}

    public CentralDividedDifferenceSolver(String functionStr, double x, double h, SolverConfig config) {
        super(functionStr, config);
        this.x = x;
        this.h = h;
        validatePositive(h);
    }

    public double firstDerivative() {
        checkExpressionValidity();
        return maybeRound(evaluateFunction(x + h) - evaluateFunction(x - h)) / (2 * h);
    }

    public double firstDerivative(double hVal) {
        checkExpressionValidity();
        validatePositive(hVal);
        return maybeRound((evaluateFunction(x + hVal) - evaluateFunction(x - hVal)) / (2 * hVal));
    }

    public double secondDerivative() {
        checkExpressionValidity();
        return maybeRound((evaluateFunction(x + h) - 2 * evaluateFunction(x) + evaluateFunction(x - h)) / (h * h));
    }

    public double secondDerivative(double hVal) {
        validatePositive(hVal);
        return maybeRound((evaluateFunction(x + hVal)
                - 2 * evaluateFunction(x)
                + evaluateFunction(x - hVal)) / (hVal * hVal));
    }

    public RichardsonExtrapolationDetails richardsonExtrapolation(int levels) {
        checkExpressionValidity();

        List<RichardsonResult> results = new ArrayList<>();
        List<RichardsonStep> firstSteps = new ArrayList<>();
        List<RichardsonStep> secondSteps = new ArrayList<>();

        RichardsonResult firstDerivResult = computeRichardsonTable(1, levels, this::firstDerivative);
        results.add(firstDerivResult);
        generateRichardsonSteps(1, firstSteps);

        RichardsonResult secondDerivResult = computeRichardsonTable(2, levels, this::secondDerivative);
        results.add(secondDerivResult);
        generateRichardsonSteps(2, secondSteps);

        return new RichardsonExtrapolationDetails(results, firstSteps, secondSteps);
    }

    protected void validatePositive(double value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Step size must be positive. Current value: " + value);
        }
    }

    private RichardsonResult computeRichardsonTable(
            int order,
            int levels,
            java.util.function.Function<Double, Double> derivativeFunc
    ) {
        double[][] D = new double[levels][levels];

        for (int i = 0; i < levels; i++) {
            double hi = maybeRound(h / Math.pow(2, i));
            D[i][0] = maybeRound(derivativeFunc.apply(hi));
        }

        for (int j = 1; j < levels; j++) {
            for (int i = j; i < levels; i++) {
                D[i][j] = maybeRound(D[i][j - 1]
                        + (D[i][j - 1] - D[i - 1][j - 1])
                        / (Math.pow(2, 2 * j) - 1));
            }
        }

        double best = D[levels - 1][levels - 1];
        double prevBest = D[levels - 1][levels - 2];
        double error = maybeRound(Math.abs(best - prevBest));

        return new RichardsonResult(order, maybeRound(h / Math.pow(2, levels - 1)), best, error);
    }

    private void generateRichardsonSteps(int order, List<RichardsonStep> steps) {
        double h1 = h;
        double h2 = h / 2;

        if (order == 1) {
            double d1 = firstDerivative(h1);
            double d2 = firstDerivative(h2);
            double factor = 4.0/3.0;
            double factor2 = 1.0/3.0;
            double extrapolated = factor * d2 - factor2 * d1;

            steps.add(new RichardsonStep(
                    "Initial Approximation",
                    "D(h) = [f(x+h) - f(x-h)] / (2h)",
                    String.format("D(%s) = %s", h1, d1),
                    d1
            ));

            steps.add(new RichardsonStep(
                    "Halved Step Size",
                    "D(h/2) = [f(x+h/2) - f(x-h/2)] / h",
                    String.format("D(%s) = %s", h2, d2),
                    d2
            ));

            steps.add(new RichardsonStep(
                    "Richardson Extrapolation",
                    "D = (4/3)D(h/2) - (1/3)D(h)",
                    String.format("D = (4/3)×%s - (1/3)×%s = %s", d2, d1, extrapolated),
                    extrapolated
            ));
        } else {
            double d1 = secondDerivative(h1);
            double d2 = secondDerivative(h2);
            double factor = 16.0/15.0;
            double factor2 = 1.0/15.0;
            double extrapolated = factor * d2 - factor2 * d1;

            steps.add(new RichardsonStep(
                    "Initial Approximation",
                    "D(h) = [f(x+h) - 2f(x) + f(x-h)] / h²",
                    String.format("D(%s) = %s", h1, d1),
                    d1
            ));

            steps.add(new RichardsonStep(
                    "Halved Step Size",
                    "D(h/2) = [f(x+h/2) - 2f(x) + f(x-h/2)] / (h/2)²",
                    String.format("D(%s) = %s", h2, d2),
                    d2
            ));

            steps.add(new RichardsonStep(
                    "Richardson Extrapolation",
                    "D = (16/15)D(h/2) - (1/15)D(h)",
                    String.format("D = (16/15)×%s - (1/15)×%s = %s", d2, d1, extrapolated),
                    extrapolated
            ));
        }
    }

    public double estimateTruncationError() {
        checkExpressionValidity();
        double h2 = maybeRound(h / 2);
        double d1 = firstDerivative();
        double d2 = firstDerivative(h2);
        return maybeRound(Math.abs(d2 - d1) / 3);
    }

    public DetailedDerivativeResult getDetailedDerivatives() {
        checkExpressionValidity();

        double xn_minus_1 = maybeRound(x - h);
        double xn = x;
        double xn_plus_1 = maybeRound(x + h);

        double f_xn_minus_1 = evaluateFunction(xn_minus_1);
        double f_xn = evaluateFunction(xn);
        double f_xn_plus_1 = evaluateFunction(xn_plus_1);

        double firstDeriv = firstDerivative();
        double secondDeriv = secondDerivative();
        double truncError = estimateTruncationError();

        String firstDerivativeFormula = "f'(x) = [f(x+h) - f(x-h)] / (2h)";
        String secondDerivativeFormula = "f''(x) = [f(x+h) - 2f(x) + f(x-h)] / h²";

        String firstDerivativeCalculation = String.format(
                "f'(%s) = [f(%s) - f(%s)] / (2×%s) = [%s - %s] / %s = %s",
                x, xn_plus_1, xn_minus_1, h, maybeRound(f_xn_plus_1), maybeRound(f_xn_minus_1), 2 * h, firstDeriv
        );

        String secondDerivativeCalculation = String.format(
                "f''(%s) = [f(%s) - 2f(%s) + f(%s)] / %s² = [%s - 2×%s + %s] / %s = %s",
                x, xn_plus_1, xn, xn_minus_1, h, maybeRound(f_xn_plus_1), maybeRound(f_xn), maybeRound(f_xn_minus_1), h * h, secondDeriv
        );

        return new DetailedDerivativeResult(
                x, h,
                xn_minus_1, xn, xn_plus_1,
                f_xn_minus_1, f_xn, f_xn_plus_1,
                firstDeriv, secondDeriv, truncError,
                firstDerivativeFormula, secondDerivativeFormula,
                firstDerivativeCalculation, secondDerivativeCalculation
        );
    }

    public double getX() { return x; }
    public double getH() { return h; }
    public void setX(double x) { this.x = x; }

    @Override
    public Object solve() {
        return getDetailedDerivatives();
    }
}