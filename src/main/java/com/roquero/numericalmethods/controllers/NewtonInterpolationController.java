package com.roquero.numericalmethods.controllers;

import com.roquero.numericalmethods.components.CustomTableView;
import com.roquero.numericalmethods.math.NewtonInterpolationSolver;
import com.roquero.numericalmethods.math.SolverConfig;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.List;

public class NewtonInterpolationController {

    @FXML private TextField xPointsField;
    @FXML private TextField yPointsField;
    @FXML private TextField interpolateXField;
    @FXML private Button loadPointsButton;
    @FXML private Button interpolateButton;
    @FXML private Button clearButton;
    @FXML private VBox pointsCard;
    @FXML private VBox tableCard;
    @FXML private VBox interpolationCard;
    @FXML private Label nonSimplifiedPolynomialLabel;
    @FXML private Label simplifiedPolynomialLabel;
    @FXML private Label interpolationResult;
    @FXML private CustomTableView dividedDiffTable;
    @FXML private CustomTableView interpolationTable;

    private NewtonInterpolationSolver solver;
    private double[] currentXPoints;
    private double[] currentYPoints;

    private MainController mainController;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupEventHandlers();
        pointsCard.setVisible(false);
        pointsCard.setManaged(false);
        tableCard.setVisible(false);
        tableCard.setManaged(false);
        interpolationCard.setVisible(false);
        interpolationCard.setManaged(false);
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void setupTableColumns() {
        dividedDiffTable.addColumn("xᵢ", 100);
        dividedDiffTable.addColumn("f[xᵢ]", 120);
        dividedDiffTable.addColumn("f[xᵢ,xᵢ₊₁]", 140);
        dividedDiffTable.addColumn("f[xᵢ,xᵢ₊₁,xᵢ₊₂]", 160);
        dividedDiffTable.addColumn("f[xᵢ,...,xᵢ₊₃]", 180);
        dividedDiffTable.addColumn("f[xᵢ,...,xᵢ₊₄]", 200);

        interpolationTable.addColumn("X Value", 150);
        interpolationTable.addColumn("Interpolated Y", 150);
    }

    private void setupEventHandlers() {
        loadPointsButton.setOnAction(e -> loadPoints());
        interpolateButton.setOnAction(e -> interpolate());
        clearButton.setOnAction(e -> clearAll());
    }

    private SolverConfig getCurrentConfig() {
        if (mainController != null) {
            return mainController.buildSolverConfig();
        }
        return SolverConfig.getDefault();
    }

    private void loadPoints() {
        try {
            SolverConfig currentConfig = getCurrentConfig();

            String xText = xPointsField.getText().trim();
            String yText = yPointsField.getText().trim();

            if (xText.isEmpty() || yText.isEmpty()) {
                showAlert("Error", "Please enter both X and Y points.");
                return;
            }

            String[] xStr = xText.split("[,\\s]+");
            String[] yStr = yText.split("[,\\s]+");

            if (xStr.length != yStr.length) {
                showAlert("Error", "Number of X and Y points must be equal.");
                return;
            }

            if (xStr.length < 2) {
                showAlert("Error", "At least 2 points are required.");
                return;
            }

            if (xStr.length > 6) {
                showAlert("Warning", "Maximum 6 points recommended for performance.");
            }

            currentXPoints = new double[xStr.length];
            currentYPoints = new double[yStr.length];

            for (int i = 0; i < xStr.length; i++) {
                currentXPoints[i] = Double.parseDouble(xStr[i].trim());
                currentYPoints[i] = Double.parseDouble(yStr[i].trim());
            }

            solver = new NewtonInterpolationSolver(currentXPoints, currentYPoints, currentConfig);

            displayDividedDifferences();
            displayPolynomials();

            pointsCard.setVisible(true);
            pointsCard.setManaged(true);
            tableCard.setVisible(true);
            tableCard.setManaged(true);
            interpolationCard.setVisible(true);
            interpolationCard.setManaged(true);

        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numeric values.");
        } catch (IllegalArgumentException e) {
            showAlert("Error", e.getMessage());
        } catch (Exception e) {
            showAlert("Error", "An unexpected error occurred:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayDividedDifferences() {
        ObservableList<ObservableList<String>> tableData = FXCollections.observableArrayList();

        int n = currentXPoints.length;
        double[][] divDiff = solver.getDividedDifferencesTable();

        for (int i = 0; i < n; i++) {
            ObservableList<String> row = FXCollections.observableArrayList();

            row.add(String.valueOf(currentXPoints[i]));

            for (int j = 0; j < n - i; j++) {
                row.add(String.valueOf(divDiff[i][j]));
            }

            for (int j = n - i; j < 6; j++) {
                row.add("—");
            }

            tableData.add(row);
        }

        dividedDiffTable.setData(tableData);
    }

    private void displayPolynomials() {
        String nonSimplified = solver.getPolynomialString();
        nonSimplifiedPolynomialLabel.setText(nonSimplified);
        nonSimplifiedPolynomialLabel.getStyleClass().add("code-text");

        String simplified = solver.getSimplifiedPolynomialString();
        simplifiedPolynomialLabel.setText(simplified);
        simplifiedPolynomialLabel.getStyleClass().add("code-text-success");
    }

    private void interpolate() {
        try {
            String xText = interpolateXField.getText().trim();
            if (xText.isEmpty()) {
                showAlert("Error", "Please enter X value(s) to interpolate.");
                return;
            }

            String[] xStr = xText.split("[,\\s]+");
            double[] xValues = new double[xStr.length];

            for (int i = 0; i < xStr.length; i++) {
                xValues[i] = Double.parseDouble(xStr[i].trim());
            }

            solver.clearInterpolationPoints();
            double[] results = solver.interpolate(xValues);

            // Display results
            StringBuilder resultText = new StringBuilder();
            for (int i = 0; i < xValues.length; i++) {
                resultText.append("f(")
                        .append(xValues[i])
                        .append(") = ")
                        .append(results[i]);
                if (i < xValues.length - 1) {
                    resultText.append("\n");
                }
            }
            interpolationResult.setText(resultText.toString());
            interpolationResult.getStyleClass().add("code-text-success");

            displayInterpolationTable();

        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numeric values.");
        } catch (Exception e) {
            showAlert("Error", "An error occurred during interpolation:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayInterpolationTable() {
        ObservableList<ObservableList<String>> tableData = FXCollections.observableArrayList();

        List<Double> points = solver.getInterpolationPoints();
        List<Double> results = solver.getInterpolationResults();

        for (int i = 0; i < points.size(); i++) {
            ObservableList<String> row = FXCollections.observableArrayList();
            row.add(String.valueOf(points.get(i)));
            row.add(String.valueOf(results.get(i)));
            tableData.add(row);
        }

        interpolationTable.setData(tableData);
    }

    private void clearAll() {
        xPointsField.setText("");
        yPointsField.setText("");
        interpolateXField.setText("");

        pointsCard.setVisible(false);
        pointsCard.setManaged(false);
        tableCard.setVisible(false);
        tableCard.setManaged(false);
        interpolationCard.setVisible(false);
        interpolationCard.setManaged(false);

        dividedDiffTable.clearData();
        interpolationTable.clearData();

        solver = null;
        currentXPoints = null;
        currentYPoints = null;
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}