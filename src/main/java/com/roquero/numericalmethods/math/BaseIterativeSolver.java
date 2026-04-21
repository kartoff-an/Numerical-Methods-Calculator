package com.roquero.numericalmethods.math;

/**
 * Base class for iterative solvers that need tolerance and max iterations.
 */
public abstract class BaseIterativeSolver extends BaseFunctionSolver {

    protected double tolerance = 1e-6;
    protected int maxIterations = 100;

    protected BaseIterativeSolver(String functionStr, SolverConfig config) {
        super(functionStr, config);
    }

    public void setTolerance(double tolerance) {
        if (tolerance <= 0) {
            throw new IllegalArgumentException("Tolerance must be positive");
        }
        this.tolerance = tolerance;
    }

    public void setMaxIterations(int maxIterations) {
        if (maxIterations <= 0) {
            throw new IllegalArgumentException("Max iterations must be positive");
        }
        this.maxIterations = maxIterations;
    }
}