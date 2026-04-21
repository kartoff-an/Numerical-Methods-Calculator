package com.roquero.numericalmethods.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Base class for all solvers that need configuration settings.
 * This provides rounding capabilities without assuming expression evaluation.
 */
public abstract class BaseConfigurableSolver {

    protected SolverConfig config;

    protected BaseConfigurableSolver(SolverConfig config) {
        this.config = (config != null) ? config : SolverConfig.getDefault();
    }

    protected int getDecimalPlaces() {
        return config.getDecimalPlaces();
    }

    protected boolean isRoundEachIteration() {
        return config.isRoundEachIteration();
    }

    protected double round(double value) {
        int dp = getDecimalPlaces();
        if (dp < 0) return value;

        return BigDecimal.valueOf(value)
                .setScale(dp, RoundingMode.HALF_EVEN)
                .doubleValue();
    }

    protected double maybeRound(double value) {
        return isRoundEachIteration() ? round(value) : value;
    }

    public SolverConfig getConfig() {
        return config;
    }

    public abstract Object solve();
}