package com.roquero.numericalmethods.controllers;

import com.roquero.numericalmethods.components.CustomTableView;
import com.roquero.numericalmethods.math.SimpsonSolver;
import com.roquero.numericalmethods.math.SolverConfig;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SimpsonController {

    @FXML private TextField functionField;
    @FXML private TextField lowerBoundField;
    @FXML private TextField upperBoundField;
    @FXML private TextField segmentsField;
    @FXML private Button solveButton;
    @FXML private Button clearButton;
    @FXML private VBox resultCard;
    @FXML private VBox detailedSolutionCard;
    @FXML private VBox tableCard;
    @FXML private Label integralValue;
    @FXML private Label segmentsUsed;
    @FXML private Label stepSizeValue;
    @FXML private Label convergedStatus;
    @FXML private CustomTableView calculationTable;

    @FXML private VBox formulaContainer;
    @FXML private VBox calculationContainer;

    private MainController mainController;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupEventHandlers();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void setupTableColumns() {
        calculationTable.addColumn("i", 50);
        calculationTable.addColumn("xᵢ", 150);
        calculationTable.addColumn("f(xᵢ)", 150);
        calculationTable.addColumn("Coefficient", 150);
        calculationTable.addColumn("cᵢ·f(xᵢ)", 150);
    }

    private void setupEventHandlers() {
        solveButton.setOnAction(e -> solve());
        clearButton.setOnAction(e -> clearAll());
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
            double a = parseDouble(lowerBoundField.getText(), 0);
            double b = parseDouble(upperBoundField.getText(), 1);
            int n = parseInt(segmentsField.getText());

            if (functionStr.isEmpty()) {
                showAlert("Function f(x) cannot be empty.");
                return;
            }

            if (a >= b) {
                showAlert("Lower bound must be less than upper bound.");
                return;
            }

            if (n < 2 || n > 1000) {
                showAlert("Number of segments must be at least 2.");
                return;
            }

            if (n % 2 != 0) {
                showAlert("Number of segments must be EVEN for Simpson's 1/3 Rule.\nCurrent value: " + n);
                return;
            }

            SimpsonSolver solver = new SimpsonSolver(functionStr, a, b, n, currentConfig);
            SimpsonSolver.Result result = solver.solveWithFixedSegments();

            displayResults(result, a, b, n);

        } catch (IllegalArgumentException e) {
            showAlert(e.getMessage());
        } catch (Exception e) {
            showAlert("An unexpected error occurred:\n" + e.getMessage());
        }
    }

    private void displayResults(SimpsonSolver.Result result, double a, double b, int n) {
        resultCard.setVisible(true);
        resultCard.setManaged(true);
        detailedSolutionCard.setVisible(true);
        detailedSolutionCard.setManaged(true);
        tableCard.setVisible(true);
        tableCard.setManaged(true);

        integralValue.setText(String.valueOf(result.integral()));
        segmentsUsed.setText(String.valueOf(result.segments()));
        stepSizeValue.setText(String.valueOf((b - a) / n));

        convergedStatus.setText("✓ Complete");
        convergedStatus.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");

        buildFormulaCard(a, b, n, result);
        buildCalculationSummaryCard(result);

        populateTable(result);
    }

    private void buildFormulaCard(double a, double b, int n, SimpsonSolver.Result result) {
        formulaContainer.getChildren().clear();

        double h = (b - a) / n;

        // Title
        Label titleLabel = new Label("Simpson's 1/3 Rule Formula");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b; -fx-padding: 0 0 8 0;");
        formulaContainer.getChildren().add(titleLabel);

        // Formula card
        VBox formulaCard = new VBox(8);
        formulaCard.setStyle("""
            -fx-background-color: #f8fafc;
            -fx-border-color: #e2e8f0;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
            -fx-padding: 14;
        """);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("∫");
        icon.setStyle("-fx-text-fill: #0284c7; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label formulaTitle = new Label("General Formula");
        formulaTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0284c7;");

        header.getChildren().addAll(icon, formulaTitle);

        Label generalFormula = new Label("∫ₐᵇ f(x)dx ≈ (h/3) [f(x₀) + f(xₙ) + 4∑f(x_odd) + 2∑f(x_even)]");
        generalFormula.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 12px; -fx-text-fill: #475569; -fx-padding: 0 0 0 24;");
        generalFormula.setWrapText(true);

        Label appliedFormula = new Label("Applied: I = " + "(" + h + "/3) × " + result.simpsonSum());
        appliedFormula.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 12px; -fx-text-fill: #475569; -fx-padding: 0 0 0 24;");
        appliedFormula.setWrapText(true);

        formulaCard.getChildren().addAll(header, generalFormula, appliedFormula);
        formulaContainer.getChildren().add(formulaCard);
    }

    private void buildCalculationSummaryCard(SimpsonSolver.Result result) {
        calculationContainer.getChildren().clear();

        // Title
        Label titleLabel = new Label("Calculation Summary");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b; -fx-padding: 0 0 8 0;");
        calculationContainer.getChildren().add(titleLabel);

        // Summary card
        VBox summaryCard = new VBox(8);
        summaryCard.setStyle("""
            -fx-background-color: #f8fafc;
            -fx-border-color: #e2e8f0;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
            -fx-padding: 14;
            -fx-margin: 10 0 0 0;
        """);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("Σ");
        icon.setStyle("-fx-text-fill: #7c3aed; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label summaryTitle = new Label("Weighted Sum");
        summaryTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #7c3aed;");

        header.getChildren().addAll(icon, summaryTitle);

        Label sumLabel = new Label("∑ cᵢ·f(xᵢ) = " + result.simpsonSum());
        sumLabel.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #6d28d9; -fx-padding: 5 0 0 24;");

        summaryCard.getChildren().addAll(header, sumLabel);
        calculationContainer.getChildren().add(summaryCard);

        // Result card
        VBox resultBox = new VBox(8);
        resultBox.setStyle("""
            -fx-background-color: #f0fdf4;
            -fx-border-color: #86efac;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
            -fx-padding: 14;
        """);

        HBox resultHeader = new HBox(8);
        resultHeader.setAlignment(Pos.CENTER_LEFT);

        Label resultIcon = new Label("✓");
        resultIcon.setStyle("-fx-text-fill: #059669; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label resultTitle = new Label("Final Result");
        resultTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #059669;");

        resultHeader.getChildren().addAll(resultIcon, resultTitle);

        Label finalResult = new Label("I = " + result.integral());
        finalResult.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 13px; -fx-text-fill: #065f46; -fx-padding: 0 0 0 24;");

        resultBox.getChildren().addAll(resultHeader, finalResult);
        calculationContainer.getChildren().add(resultBox);

        VBox.setMargin(resultBox, new Insets(10, 0, 0, 0));
    }

    private void populateTable(SimpsonSolver.Result result) {
        ObservableList<ObservableList<String>> tableData = FXCollections.observableArrayList();

        for (SimpsonSolver.Segment segment : result.calculationDetails()) {
            ObservableList<String> row = FXCollections.observableArrayList();

            String coefficient;
            if (segment.coefficient() == 1) {
                coefficient = "1 (endpoint)";
            } else if (segment.coefficient() == 2) {
                coefficient = "2 (even)";
            } else {
                coefficient = "4 (odd)";
            }

            row.add(String.valueOf(segment.index()));
            row.add(String.format(String.valueOf(segment.x())));
            row.add(String.format(String.valueOf(segment.fx())));
            row.add(coefficient);
            row.add(String.valueOf(segment.fx() * segment.coefficient()));

            tableData.add(row);
        }

        ObservableList<String> emptyRow = FXCollections.observableArrayList();
        emptyRow.addAll("", "", "", "", "");
        tableData.add(emptyRow);

        ObservableList<String> sumRow = FXCollections.observableArrayList();
        sumRow.add("");
        sumRow.add("");
        sumRow.add("");
        sumRow.add("Σ =");
        sumRow.add(String.valueOf(result.simpsonSum()));
        tableData.add(sumRow);

        calculationTable.setData(tableData);
    }

    private void clearAll() {
        functionField.setText("");
        lowerBoundField.setText("");
        upperBoundField.setText("");
        segmentsField.setText("");

        resultCard.setVisible(false);
        resultCard.setManaged(false);
        detailedSolutionCard.setVisible(false);
        detailedSolutionCard.setManaged(false);
        tableCard.setVisible(false);
        tableCard.setManaged(false);
        calculationTable.clearData();

        formulaContainer.getChildren().clear();
        calculationContainer.getChildren().clear();
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
            return 4;
        }
        try {
            int value = Integer.parseInt(text.trim());
            return Math.max(2, Math.min(value, 1000));
        } catch (NumberFormatException e) {
            return 4;
        }
    }

    private void showAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}