package com.roquero.numericalmethods.controllers;

import com.roquero.numericalmethods.components.CustomTableView;
import com.roquero.numericalmethods.math.NewtonRaphsonSolver;
import com.roquero.numericalmethods.math.SolverConfig;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class NewtonRaphsonController {

    @FXML private TextField functionField;
    @FXML private TextField initialField;
    @FXML private TextField toleranceField;
    @FXML private TextField maxIterationsField;
    @FXML private Button solveButton;
    @FXML private Button clearButton;
    @FXML private VBox resultCard;
    @FXML private VBox tableCard;
    @FXML private Label rootValue;
    @FXML private Label convergedStatus;
    @FXML private Label convergenceTest;
    @FXML private Label iterationsCount;
    @FXML private Label finalError;
    @FXML private CustomTableView iterationsTable;

    private MainController mainController;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupEventHandlers();
        setDefaultValues();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void setupTableColumns() {
        iterationsTable.addColumn("n", 80);
        iterationsTable.addColumn("xₙ", 140);
        iterationsTable.addColumn("f(x)", 140);
        iterationsTable.addColumn("f'(x)", 140);
        iterationsTable.addColumn("xₙ₊₁", 140);
        iterationsTable.addColumn("Error", 140);
    }

    private void setupEventHandlers() {
        solveButton.setOnAction(e -> solve());
        clearButton.setOnAction(e -> clearAll());
    }

    private void setDefaultValues() {
        functionField.setText("x^2 - 4");
        initialField.setText("3.0");
        toleranceField.setText("1e-6");
        maxIterationsField.setText("100");
    }

    private SolverConfig getCurrentConfig() {
        if (mainController != null) {
            return mainController.buildSolverConfig();
        }
        return SolverConfig.getDefault();
    }

    private void solve() {
        try {
            SolverConfig currentConfig = getCurrentConfig();

            String functionStr = functionField.getText().trim();
            double initial = parseDouble(initialField.getText(), 2.0);
            double tolerance = parseDouble(toleranceField.getText(), 1e-6);
            int maxIterations = parseInt(maxIterationsField.getText());

            if (functionStr.isEmpty()) {
                showAlert("Function f(x) cannot be empty.");
                return;
            }
            if (maxIterations > 1000) {
                showAlert("Max iterations limited to 1000 for performance.");
                maxIterations = 1000;
            }

            NewtonRaphsonSolver solver = new NewtonRaphsonSolver(functionStr, initial, currentConfig);

            if (!solver.isExpressionValid()) {
                showAlert("Invalid mathematical expression:\n" + solver.getValidationError() +
                                "\n\nValid examples:\n• x^2 - 4\n• sin(x) + cos(x)\n• exp(x) - 2*x\n• log(x) - 1");
                return;
            }

            solver.setTolerance(tolerance);
            solver.setMaxIterations(maxIterations);

            NewtonRaphsonSolver.Result result = solver.solve();

            displayResults(result);

        } catch (IllegalArgumentException e) {
            showAlert(e.getMessage());
        } catch (Exception e) {
            showAlert("An unexpected error occurred:\n" + e.getMessage());
        }
    }

    private void displayResults(NewtonRaphsonSolver.Result result) {
        resultCard.setVisible(true);
        resultCard.setManaged(true);

        if (result.converged()) {
            rootValue.setText(String.valueOf(result.root()));
            convergedStatus.setText("✓ Yes");
            convergedStatus.getStyleClass().add("text-success");
        } else {
            rootValue.setText("Failed");
            convergedStatus.setText("✗ No");
            convergedStatus.getStyleClass().add("text-error");
        }

        convergenceTest.setText(String.valueOf(result.convergenceTest()));

        int iterCount = result.iterations().size();
        iterationsCount.setText(String.valueOf(iterCount));

        if (iterCount > 0 && result.converged()) {
            double lastError = result.iterations().get(iterCount - 1).error();
            finalError.setText(String.valueOf(lastError));
        } else {
            finalError.setText("N/A");
        }

        if (iterCount > 0) {
            tableCard.setVisible(true);
            tableCard.setManaged(true);
            populateTable(result);
        } else {
            tableCard.setVisible(false);
            tableCard.setManaged(false);
        }
    }

    private void populateTable(NewtonRaphsonSolver.Result result) {
        ObservableList<ObservableList<String>> tableData = FXCollections.observableArrayList();

        for (NewtonRaphsonSolver.Iteration iter : result.iterations()) {
            ObservableList<String> row = FXCollections.observableArrayList();
            row.add(String.valueOf(iter.iteration()));
            row.add(String.valueOf(iter.xn()));
            row.add(String.valueOf(iter.fx()));
            row.add(String.valueOf(iter.dfx()));
            row.add(String.valueOf(iter.xn1()));
            row.add(String.valueOf(iter.error()));
            tableData.add(row);
        }

        iterationsTable.setData(tableData);
    }

    private void clearAll() {
        functionField.setText("");
        initialField.setText("");
        toleranceField.setText("");
        maxIterationsField.setText("");

        resultCard.setVisible(false);
        resultCard.setManaged(false);
        tableCard.setVisible(false);
        tableCard.setManaged(false);
        iterationsTable.clearData();
    }

    private double parseDouble(String text, double defaultValue) {
        if (text == null || text.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private int parseInt(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 100;
        }
        try {
            int value = Integer.parseInt(text.trim());
            return Math.max(1, Math.min(value, 1000));
        } catch (NumberFormatException e) {
            return 100;
        }
    }

    private void showAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}