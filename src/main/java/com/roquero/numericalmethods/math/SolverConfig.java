package com.roquero.numericalmethods.math;

public class SolverConfig {

    private static final SolverConfig DEFAULT = new SolverConfig();

    private int decimalPlaces = 5;
    private boolean roundEachIteration = true;

    public SolverConfig() {}

    public SolverConfig(int decimalPlaces, boolean roundEachIteration) {
        this.decimalPlaces = decimalPlaces;
        this.roundEachIteration = roundEachIteration;
    }

    public static SolverConfig getDefault() {
        return DEFAULT;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public boolean isRoundEachIteration() {
        return roundEachIteration;
    }
}