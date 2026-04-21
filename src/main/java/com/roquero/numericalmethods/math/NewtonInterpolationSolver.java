package com.roquero.numericalmethods.math;

import java.util.ArrayList;
import java.util.List;

public class NewtonInterpolationSolver extends BaseConfigurableSolver {

    private final double[] xValues;
    private final double[] yValues;
    private final double[][] dividedDifferences;
    private final List<Double> interpolationPoints;
    private final List<Double> interpolationResults;

    public record DividedDifferenceRow(
            int level,
            double[] values
    ) {}

    public record Result(
            double[] xValues,
            double[] yValues,
            double[][] dividedDifferencesTable,
            List<Double> interpolationPoints,
            List<Double> interpolationResults,
            String polynomial
    ) {}

    public NewtonInterpolationSolver(double[] xValues, double[] yValues, SolverConfig config) {
        super(config);

        if (xValues.length != yValues.length) {
            throw new IllegalArgumentException("X and Y arrays must have the same length");
        }
        if (xValues.length < 2) {
            throw new IllegalArgumentException("At least 2 points are required for interpolation");
        }

        this.xValues = xValues.clone();
        this.yValues = yValues.clone();
        this.dividedDifferences = new double[xValues.length][xValues.length];
        this.interpolationPoints = new ArrayList<>();
        this.interpolationResults = new ArrayList<>();

        calculateDividedDifferences();
    }

    private void calculateDividedDifferences() {
        int n = xValues.length;

        for (int i = 0; i < n; i++) {
            dividedDifferences[i][0] = yValues[i];
        }

        for (int j = 1; j < n; j++) {
            for (int i = 0; i < n - j; i++) {
                dividedDifferences[i][j] = maybeRound((dividedDifferences[i + 1][j - 1] - dividedDifferences[i][j - 1])
                        / (xValues[i + j] - xValues[i]));
            }
        }
    }

    public double evaluate(double x) {
        int n = xValues.length;
        double result = maybeRound(dividedDifferences[0][0]);
        double product = 1.0;

        for (int i = 1; i < n; i++) {
            product = maybeRound(product * (x - xValues[i - 1]));
            result = maybeRound(result + dividedDifferences[0][i] * product);
        }

        return result;
    }

    public double[] interpolate(double[] points) {
        double[] results = new double[points.length];
        for (int i = 0; i < points.length; i++) {
            results[i] = maybeRound(evaluate(points[i]));
            interpolationPoints.add(maybeRound(points[i]));
            interpolationResults.add(maybeRound(results[i]));
        }
        return results;
    }

    public void clearInterpolationPoints() {
        interpolationPoints.clear();
        interpolationResults.clear();
    }

    public String getPolynomialString() {
        int n = xValues.length;
        StringBuilder polynomial = new StringBuilder();

        polynomial.append(dividedDifferences[0][0]);

        for (int i = 1; i < n; i++) {
            StringBuilder term = new StringBuilder();
            term.append(String.format(" + (%s)", dividedDifferences[0][i]));

            for (int j = 0; j < i; j++) {
                if (xValues[j] >= 0) {
                    term.append(String.format("(x - %s)", xValues[j]));
                } else {
                    term.append(String.format("(x + %s)", Math.abs(xValues[j])));
                }
            }

            polynomial.append(term);
        }

        return "P(x) = " + polynomial;
    }

    public String getSimplifiedPolynomialString() {
        int n = xValues.length;
        double[] coefficients = getPolynomialCoefficients();
        StringBuilder polynomial = new StringBuilder();

        for (int i = n - 1; i >= 0; i--) {
            if (Math.abs(coefficients[i]) < 1e-10) continue;

            if (!polynomial.isEmpty()) {
                polynomial.append(coefficients[i] > 0 ? " + " : " - ");
            } else if (coefficients[i] < 0) {
                polynomial.append("-");
            }

            double absCoef = Math.abs(coefficients[i]);

            if (i == 0) {
                polynomial.append(absCoef);
            } else if (i == 1) {
                if (Math.abs(absCoef - 1.0) < 1e-10) {
                    polynomial.append("x");
                } else {
                    polynomial.append(absCoef)
                            .append(" x");
                }
            } else {
                if (Math.abs(absCoef - 1.0) < 1e-10) {
                    polynomial.append(String.format("x^%d", i));
                } else {
                    polynomial.append(absCoef)
                            .append(String.format(" x^%d", i));
                }
            }
        }

        return "P(x) = " + (polynomial.isEmpty() ? "0" : polynomial.toString());
    }

    public double[] getPolynomialCoefficients() {
        int n = xValues.length;
        double[] coefficients = new double[n];

        coefficients[0] = dividedDifferences[0][0];

        for (int i = 1; i < n; i++) {
            double[] product = new double[i + 1];
            product[0] = 1.0;

            for (int j = 0; j < i; j++) {
                double[] newProduct = new double[j + 2];
                for (int k = 0; k <= j; k++) {
                    newProduct[k] = maybeRound(newProduct[k] + product[k] * (-xValues[j]));
                    newProduct[k + 1] = maybeRound(newProduct[k + 1] + product[k]);
                }
                product = newProduct;
            }

            for (int j = 0; j <= i; j++) {
                coefficients[j] = maybeRound(coefficients[j] + dividedDifferences[0][i] * product[j]);
            }
        }

        return coefficients;
    }

    public double[][] getDividedDifferencesTable() {
        return dividedDifferences.clone();
    }

    public Result getResult() {
        return new Result(
                xValues.clone(),
                yValues.clone(),
                dividedDifferences.clone(),
                new ArrayList<>(interpolationPoints),
                new ArrayList<>(interpolationResults),
                getSimplifiedPolynomialString()
        );
    }

    @Override
    public Object solve() {
        return getResult();
    }

    public List<Double> getInterpolationPoints() { return new ArrayList<>(interpolationPoints); }
    public List<Double> getInterpolationResults() { return new ArrayList<>(interpolationResults); }
}