package com.roquero.numericalmethods.math;

import java.util.ArrayList;
import java.util.List;

public class SimpsonSolver extends BaseIterativeSolver {

    private final double a;
    private final double b;
    private int n;
    private double simpsonSum = 0.0;

    public record Segment(
            int index,
            double x,
            double fx,
            double coefficient
    ) {}

    public record Result(
            double integral,
            double approximateError,
            int segments,
            List<Segment> calculationDetails,
            double simpsonSum,
            boolean converged
    ) {}

    private record SimpsonCalculation(
            double integral,
            List<Segment> segments
    ) {}

    public SimpsonSolver(String functionStr, double a, double b, int n, SolverConfig config) {
        super(functionStr, config);
        this.a = a;
        this.b = b;
        this.n = n;
    }

    @Override
    public Result solve() {
        return solveWithTolerance(tolerance, maxIterations);
    }

    public Result solveWithTolerance(double tolerance, int maxIterations) {
        checkExpressionValidity();
        validateEven(n);

        SimpsonCalculation calculation = calculateSimpson(n);
        double error = calculateApproximateError(tolerance, maxIterations);

        return new Result(
                calculation.integral,
                error,
                n,
                calculation.segments,
                simpsonSum,
                error < tolerance);
    }

    public Result solveWithFixedSegments() {
        checkExpressionValidity();
        validateEven(n);

        SimpsonCalculation calculation = calculateSimpson(n);
        return new Result(
                calculation.integral,
                0,
                n,
                calculation.segments,
                simpsonSum,
                true);
    }

    private SimpsonCalculation calculateSimpson(int segments) {
        validateEven(segments);

        List<Segment> segmentList = new ArrayList<>();
        double h = maybeRound((b - a) / segments);

        for (int i = 0; i <= segments; i++) {
            double x = maybeRound(a + i * h);
            double fx = maybeRound(evaluateFunction(x));
            double coefficient = getCoefficient(i, segments);

            simpsonSum += maybeRound(coefficient * fx);
            segmentList.add(new Segment(i, x, fx, coefficient));
        }

        double integral = maybeRound((h / 3) * simpsonSum);
        return new SimpsonCalculation(integral, segmentList);
    }

    private double getCoefficient(int i, int totalSegments) {
        if (i == 0 || i == totalSegments) {
            return 1;
        } else if (i % 2 == 0) {
            return 2;
        } else {
            return 4;
        }
    }

    private double calculateComposite(int segments) {
        validateEven(segments);

        double h = maybeRound((b - a) / segments);
        double sum = 0;

        for (int i = 0; i <= segments; i++) {
            double x = maybeRound(a + i * h);
            double fx = maybeRound(evaluateFunction(x));
            sum += maybeRound(getCoefficient(i, segments) * fx);
        }

        return maybeRound((h / 3) * sum);
    }

    private double calculateApproximateError(double tolerance, int maxIterations) {
        int currentN = n;
        double previousResult = 0;
        double currentResult = 0;

        for (int iter = 0; iter < maxIterations; iter++) {
            currentResult = maybeRound(calculateComposite(currentN));

            if (iter > 0) {
                double error = maybeRound(Math.abs(currentResult - previousResult));
                if (error < tolerance) {
                    return error;
                }
            }

            previousResult = currentResult;
            currentN = 2;
        }

        return maybeRound(Math.abs(currentResult - previousResult));
    }

    public double getA() { return a; }
    public int getN() { return n; }
    public void setN(int n) { this.n = n; }

    protected void validateEven(int value) {
        if (value % 2 != 0) {
            throw new IllegalArgumentException("Number of segments" + " must be even. Current value: " + value);
        }
    }
}