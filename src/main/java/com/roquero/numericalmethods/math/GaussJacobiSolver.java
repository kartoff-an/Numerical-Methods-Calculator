package com.roquero.numericalmethods.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GaussJacobiSolver extends BaseIterativeSolver {

    private double[][] A;
    private double[] b;
    private final double[] initialValues;

    public record Iteration(
            int iteration,
            double[] xValues,
            double error
    ) {}

    public record Result(
            double[] solution,
            List<Iteration> iterations,
            boolean converged
    ) {}

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public GaussJacobiSolver(double[][] A, double[] b, double[] initialValues, SolverConfig config) {
        super(null, config);
        this.A = A;
        this.b = b;
        this.initialValues = initialValues;
    }

    @Override
    public Result solve() {
        int size = b.length;

        if (A[0].length != initialValues.length)
            throw new IllegalArgumentException("Matrix and initial guess dimension mismatch");

        Object[] reorderedMatrix = reorderMatrix(A, b);
        A = (double[][]) reorderedMatrix[0];
        b = (double[]) reorderedMatrix[1];

        double[] x = Arrays.copyOf(initialValues, size);
        List<Iteration> iterations = new ArrayList<>();

        for (int k = 1; k <= maxIterations; k++) {
            double[] nextX = new double[size];

            for (int i = 0; i < size; i++) {
                double sum = b[i];
                for (int j = 0; j < size; j++) {
                    if (i != j) {
                        sum += - A[i][j] * x[j];
                    }
                }

                if (Math.abs(A[i][i]) < 1e-12) {
                    throw new ArithmeticException("Zero on diagonal at row " + i);
                }

                nextX[i] = maybeRound(sum / A[i][i]);
            }

            double error = 0;
            for (int i = 0; i < size; i++) {
                error = maybeRound(Math.max(error, Math.abs(nextX[i] - x[i])));
            }

            iterations.add(new Iteration(k, nextX.clone(), error));

            if (error < tolerance) {
                return new Result(nextX, iterations, true);
            }

            x = nextX;
        }

        return new Result(x, iterations, false);
    }

    public static Object[] reorderMatrix(double[][] A, double[] b) {
        int n = A.length;

        int[] perm = new int[n];
        for (int i = 0; i < n; i++) perm[i] = i;

        do {
            double[][] newA = new double[n][n];
            double[] newB = new double[n];

            for (int i = 0; i < n; i++) {
                newA[i] = Arrays.copyOf(A[perm[i]], n);
                newB[i] = b[perm[i]];
            }

            if (isDiagonallyDominant(newA)) {
                return new Object[]{newA, newB};
            }

        } while (nextPermutation(perm));

        throw new RuntimeException("No diagonally dominant arrangement found");
    }

    private static boolean nextPermutation(int[] arr) {
        int n = arr.length;

        int i = n - 2;
        while (i >= 0 && arr[i] >= arr[i + 1]) {
            i--;
        }

        if (i < 0) return false;

        int j = n - 1;
        while (arr[j] <= arr[i]) {
            j--;
        }

        swap(arr, i, j);

        reverse(arr, i + 1, n - 1);

        return true;
    }

    private static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    private static void reverse(int[] arr, int left, int right) {
        while (left < right) {
            swap(arr, left++, right--);
        }
    }

    private static boolean isDiagonallyDominant(double[][] A) {
        int n = A.length;

        for (int i = 0; i < n; i++) {
            double diag = Math.abs(A[i][i]);
            double sum = 0;

            for (int j = 0; j < n; j++) {
                if (i != j) {
                    sum += Math.abs(A[i][j]);
                }
            }

            if (diag < sum) {
                return false;
            }
        }

        return true;
    }
}
