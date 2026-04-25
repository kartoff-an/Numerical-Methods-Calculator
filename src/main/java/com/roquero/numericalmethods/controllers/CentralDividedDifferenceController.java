package com.roquero.numericalmethods.controllers;

import com.roquero.numericalmethods.components.CustomTableView;
import com.roquero.numericalmethods.math.CentralDividedDifferenceSolver;
import com.roquero.numericalmethods.math.CentralDividedDifferenceSolver.RichardsonStep;
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

import java.util.List;

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

        buildDerivativeSolutionCards(result, true);
        buildDerivativeSolutionCards(result, false);
    }

    private void buildDerivativeSolutionCards(CentralDividedDifferenceSolver.DetailedDerivativeResult result, boolean isFirst) {
        VBox container = isFirst ? firstDerivativeSolutionContainer : secondDerivativeSolutionContainer;
        container.getChildren().clear();

        String title = isFirst ? "First Derivative f'(x)" : "Second Derivative f''(x)";
        String formula = isFirst ? result.firstDerivativeFormula() : result.secondDerivativeFormula();
        String calculation = isFirst ? result.firstDerivativeCalculation() : result.secondDerivativeCalculation();

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("title");
        container.getChildren().add(titleLabel);

        VBox formulaCard = createSolutionCard("Formula", formula);
        container.getChildren().add(formulaCard);

        VBox calcCard = createSolutionCard("Calculation", calculation);
        container.getChildren().add(calcCard);

        VBox.setMargin(formulaCard, new Insets(0, 0, 8, 0));
        VBox.setMargin(calcCard, new Insets(0, 0, 8, 0));
    }

    private VBox createSolutionCard(String title, String content) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("▶");
        icon.getStyleClass().add("icon");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("title-small");

        header.getChildren().addAll(icon, titleLabel);

        Label contentLabel = new Label(content);
        contentLabel.getStyleClass().add("content-label");
        contentLabel.setWrapText(true);

        card.getChildren().addAll(header, contentLabel);

        return card;
    }

    private void showRichardsonExtrapolation() {
        if (solver == null) {
            showAlert("Please calculate derivatives first.");
            return;
        }

        try {
            var richardsonDetails = solver.richardsonExtrapolation(4);
            var results = richardsonDetails.results();

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

    private void buildStepCards(List<RichardsonStep> steps,
                                VBox container, String title) {
        container.getChildren().clear();

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("title");
        container.getChildren().add(titleLabel);

        for (CentralDividedDifferenceSolver.RichardsonStep step : steps) {
            VBox stepCard = new VBox(8);
            stepCard.getStyleClass().add("card");

            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);

            Label icon = new Label("▶");
            icon.getStyleClass().add("icon");

            Label stepLabel = new Label(step.description());
            stepLabel.getStyleClass().add("title-small");

            header.getChildren().addAll(icon, stepLabel);

            Label formulaLabel = new Label(step.formula());
            formulaLabel.getStyleClass().add("code-text");
            formulaLabel.setWrapText(true);

            Label calcLabel = new Label(step.calculation());
            calcLabel.getStyleClass().add("code-text");
            calcLabel.setWrapText(true);

            HBox resultBox = new HBox(8);
            resultBox.setAlignment(Pos.CENTER_LEFT);
            resultBox.setPadding(new Insets(5, 0, 0, 20));

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