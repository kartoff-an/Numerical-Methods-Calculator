package com.roquero.numericalmethods.controllers;

import com.roquero.numericalmethods.components.CustomTableView;
import com.roquero.numericalmethods.math.CentralDividedDifferenceSolver;
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

public class CentralDividedDifferenceController {

    @FXML private TextField functionField;
    @FXML private TextField xPointField;
    @FXML private TextField stepSizeField;
    @FXML private Button calculateButton;
    @FXML private Button clearButton;
    @FXML private Button richardsonButton;
    @FXML private VBox resultCard;
    @FXML private VBox richardsonCard;
    @FXML private Label firstDerivative;
    @FXML private Label secondDerivative;
    @FXML private Label errorEstimate;
    @FXML private CustomTableView richardsonTable;

    @FXML private VBox detailedSolutionCard;
    @FXML private VBox firstDerivativeSolutionContainer;
    @FXML private VBox secondDerivativeSolutionContainer;

    @FXML private VBox firstDerivativeStepsContainer;
    @FXML private VBox secondDerivativeStepsContainer;

    private CentralDividedDifferenceSolver solver;
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
        richardsonTable.addColumn("Derivative", 120);
        richardsonTable.addColumn("Step Size (h)", 120);
        richardsonTable.addColumn("Value", 150);
        richardsonTable.addColumn("Error", 120);
    }

    private void setupEventHandlers() {
        calculateButton.setOnAction(e -> calculate());
        clearButton.setOnAction(e -> clearAll());
        richardsonButton.setOnAction(e -> showRichardsonExtrapolation());
    }

    private SolverConfig getCurrentConfig() {
        if (mainController != null) {
            return mainController.buildSolverConfig();
        }
        return SolverConfig.getDefault();
    }

    private void calculate() {
        try {
            SolverConfig currentConfig = getCurrentConfig();

            richardsonCard.setVisible(false);
            richardsonCard.setManaged(false);
            richardsonTable.clearData();

            String functionStr = functionField.getText().trim();
            double x = parseDouble(xPointField.getText(), 1.0);
            double h = parseDouble(stepSizeField.getText(), 0.001);

            if (functionStr.isEmpty()) {
                showAlert("Function f(x) cannot be empty.");
                return;
            }

            if (h <= 0) {
                showAlert("Step size must be positive.");
                return;
            }

            solver = new CentralDividedDifferenceSolver(functionStr, x, h, currentConfig);
            CentralDividedDifferenceSolver.DetailedDerivativeResult result = solver.getDetailedDerivatives();
            displayDetailedResults(result);

        } catch (Exception e) {
            showAlert("An unexpected error occurred:\n" + e.getMessage());
        }
    }

    private void displayDetailedResults(CentralDividedDifferenceSolver.DetailedDerivativeResult result) {
        resultCard.setVisible(true);
        resultCard.setManaged(true);
        detailedSolutionCard.setVisible(true);
        detailedSolutionCard.setManaged(true);

        firstDerivative.setText(String.valueOf(result.firstDerivative()));
        secondDerivative.setText(String.valueOf(result.secondDerivative()));
        errorEstimate.setText(String.valueOf(result.truncationError()));

        styleDerivativeLabel(firstDerivative, result.firstDerivative());
        styleDerivativeLabel(secondDerivative, result.secondDerivative());

        buildDerivativeSolutionCards(result, true);
        buildDerivativeSolutionCards(result, false);
    }

    private void buildDerivativeSolutionCards(CentralDividedDifferenceSolver.DetailedDerivativeResult result, boolean isFirst) {
        VBox container = isFirst ? firstDerivativeSolutionContainer : secondDerivativeSolutionContainer;
        container.getChildren().clear();

        String title = isFirst ? "First Derivative f'(x)" : "Second Derivative f''(x)";
        String formula = isFirst ? result.firstDerivativeFormula() : result.secondDerivativeFormula();
        String calculation = isFirst ? result.firstDerivativeCalculation() : result.secondDerivativeCalculation();
        double value = isFirst ? result.firstDerivative() : result.secondDerivative();

        // Title
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b; -fx-padding: 0 0 8 0;");
        container.getChildren().add(titleLabel);

        // Formula Card
        VBox formulaCard = createSolutionCard("Formula", formula, "#0284c7");
        container.getChildren().add(formulaCard);

        // Calculation Card
        VBox calcCard = createSolutionCard("Calculation", calculation, "#059669");
        container.getChildren().add(calcCard);

        // Result Card
        VBox resultCard = createResultCard(String.valueOf(value));
        container.getChildren().add(resultCard);

        VBox.setMargin(formulaCard, new Insets(0, 0, 8, 0));
        VBox.setMargin(calcCard, new Insets(0, 0, 8, 0));
    }

    private VBox createSolutionCard(String title, String content, String color) {
        VBox card = new VBox(8);
        card.setStyle("""
            -fx-background-color: #f8fafc;
            -fx-border-color: #e2e8f0;
            -fx-border-radius: 6;
            -fx-background-radius: 6;
            -fx-padding: 12;
        """);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("▶");
        icon.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 10px;", color));

        Label titleLabel = new Label(title);
        titleLabel.setStyle(String.format("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: %s;", color));

        header.getChildren().addAll(icon, titleLabel);

        Label contentLabel = new Label(content);
        contentLabel.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 12px; -fx-text-fill: #475569;");
        contentLabel.setWrapText(true);

        card.getChildren().addAll(header, contentLabel);

        return card;
    }

    private VBox createResultCard(String value) {
        VBox card = new VBox(8);
        card.setStyle("""
            -fx-background-color: #f0fdf4;
            -fx-border-color: #86efac;
            -fx-border-radius: 6;
            -fx-background-radius: 6;
            -fx-padding: 12;
        """);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("✓");
        icon.setStyle("-fx-text-fill: #059669; -fx-font-size: 10px; -fx-font-weight: bold;");

        Label titleLabel = new Label("Result");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #059669;");

        header.getChildren().addAll(icon, titleLabel);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #065f46;");

        card.getChildren().addAll(header, valueLabel);

        return card;
    }

    private void styleDerivativeLabel(Label label, double value) {
        if (Math.abs(value) < 1e-10) {
            label.setStyle("-fx-text-fill: #9ca3af; -fx-font-weight: normal;");
        } else if (Math.abs(value) > 1000) {
            label.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        } else {
            label.setStyle("-fx-text-fill: #059669; -fx-font-weight: bold;");
        }
    }

    private void showRichardsonExtrapolation() {
        if (solver == null) {
            showAlert("Please calculate derivatives first.");
            return;
        }

        try {
            var richardsonDetails = solver.richardsonExtrapolation(4);
            var results = richardsonDetails.results();

            // Populate table
            ObservableList<ObservableList<String>> tableData = FXCollections.observableArrayList();
            for (var result : results) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(result.order() == 1 ? "f'(x)" : "f''(x)");
                row.add(String.valueOf(result.h()));
                row.add(String.valueOf(result.derivative()));
                row.add(String.valueOf(result.error()));
                tableData.add(row);
            }
            richardsonTable.setData(tableData);

            buildStepCards(richardsonDetails.firstDerivativeSteps(), firstDerivativeStepsContainer, "First Derivative");
            buildStepCards(richardsonDetails.secondDerivativeSteps(), secondDerivativeStepsContainer, "Second Derivative");

            richardsonCard.setVisible(true);
            richardsonCard.setManaged(true);

        } catch (Exception e) {
            showAlert("Richardson extrapolation failed:\n" + e.getMessage());
        }
    }

    private void buildStepCards(java.util.List<CentralDividedDifferenceSolver.RichardsonStep> steps,
                                VBox container, String title) {
        container.getChildren().clear();

        // Title
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b; -fx-padding: 0 0 8 0;");
        container.getChildren().add(titleLabel);

        for (CentralDividedDifferenceSolver.RichardsonStep step : steps) {
            VBox stepCard = new VBox(8);
            stepCard.setStyle("""
                -fx-background-color: #f8fafc;
                -fx-border-color: #e2e8f0;
                -fx-border-radius: 8;
                -fx-background-radius: 8;
                -fx-padding: 14;
            """);

            // Step header
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);

            Label icon = new Label("▶");
            icon.setStyle("-fx-text-fill: #0284c7; -fx-font-size: 10px;");

            Label stepLabel = new Label(step.description());
            stepLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0284c7;");

            header.getChildren().addAll(icon, stepLabel);

            // Formula
            Label formulaLabel = new Label(step.formula());
            formulaLabel.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 12px; -fx-text-fill: #475569; -fx-padding: 0 0 0 20;");
            formulaLabel.setWrapText(true);

            // Calculation
            Label calcLabel = new Label(step.calculation());
            calcLabel.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 11px; -fx-text-fill: #64748b; -fx-padding: 0 0 0 20;");
            calcLabel.setWrapText(true);

            // Result
            HBox resultBox = new HBox(8);
            resultBox.setAlignment(Pos.CENTER_LEFT);
            resultBox.setPadding(new Insets(5, 0, 0, 20));

            Label resultIcon = new Label("→");
            resultIcon.setStyle("-fx-text-fill: #059669; -fx-font-size: 11px; -fx-font-weight: bold;");

            Label resultLabel = new Label("Result:");
            resultLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #475569;");

            Label valueLabel = new Label(String.valueOf(step.result()));
            valueLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #059669; -fx-font-family: 'Courier New', monospace;");

            resultBox.getChildren().addAll(resultIcon, resultLabel, valueLabel);

            stepCard.getChildren().addAll(header, formulaLabel, calcLabel, resultBox);
            container.getChildren().add(stepCard);

            VBox.setMargin(stepCard, new Insets(0, 0, 10, 0));
        }
    }

    private void clearAll() {
        functionField.setText("");
        xPointField.setText("");
        stepSizeField.setText("");

        resultCard.setVisible(false);
        resultCard.setManaged(false);
        detailedSolutionCard.setVisible(false);
        detailedSolutionCard.setManaged(false);
        richardsonCard.setVisible(false);
        richardsonCard.setManaged(false);

        richardsonTable.clearData();
        firstDerivativeSolutionContainer.getChildren().clear();
        secondDerivativeSolutionContainer.getChildren().clear();
        firstDerivativeStepsContainer.getChildren().clear();
        secondDerivativeStepsContainer.getChildren().clear();

        solver = null;
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