package com.roquero.numericalmethods.controllers;

import com.roquero.numericalmethods.components.CustomTableView;
import com.roquero.numericalmethods.math.GaussJacobiSolver;
import com.roquero.numericalmethods.math.SolverConfig;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;

public class GaussJacobiController {

    @FXML private TextField matrixSizeField;
    @FXML private GridPane matrixGrid;
    @FXML private VBox matrixInputPanel;
    @FXML private Button generateMatrixButton;
    @FXML private Button solveButton;
    @FXML private Button clearButton;
    @FXML private VBox resultCard;
    @FXML private VBox tableCard;
    @FXML private Label solutionStatus;
    @FXML private Label convergedStatus;
    @FXML private Label iterationsCount;
    @FXML private Label finalError;
    @FXML private CustomTableView iterationsTable;

    @FXML private TextField toleranceField;
    @FXML private TextField maxIterationsField;

    private int currentSize = 0;
    private TextField[][] matrixFields;
    private TextField[] rhsFields;
    private TextField[] initialFields;

    private MainController mainController;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupEventHandlers();
        matrixInputPanel.setVisible(false);
        matrixInputPanel.setManaged(false);

        if (toleranceField != null) {
            toleranceField.setText("1e-6");
        }
        if (maxIterationsField != null) {
            maxIterationsField.setText("100");
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void setupTableColumns() {
        iterationsTable.addColumn("Iteration", 80);
        iterationsTable.addColumn("x₁", 120);
        iterationsTable.addColumn("x₂", 120);
        iterationsTable.addColumn("x₃", 120);
        iterationsTable.addColumn("x₄", 120);
        iterationsTable.addColumn("x₅", 120);
        iterationsTable.addColumn("Maximum Error", 150);
    }

    private void setupEventHandlers() {
        generateMatrixButton.setOnAction(e -> generateMatrixInput());
        solveButton.setOnAction(e -> solve());
        clearButton.setOnAction(e -> clearAll());
    }

    private SolverConfig getCurrentConfig() {
        if (mainController != null) {
            return mainController.buildSolverConfig();
        }
        return SolverConfig.getDefault();
    }

    private void generateMatrixInput() {
        try {
            try {
                if (toleranceField != null && !toleranceField.getText().trim().isEmpty()) {
                    double tol = Double.parseDouble(toleranceField.getText().trim());
                    if (tol <= 0) {
                        showAlert("Error", "Tolerance must be positive.");
                        return;
                    }
                }
                if (maxIterationsField != null && !maxIterationsField.getText().trim().isEmpty()) {
                    int maxIter = Integer.parseInt(maxIterationsField.getText().trim());
                    if (maxIter <= 0) {
                        showAlert("Error", "Maximum iterations must be positive.");
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid tolerance or max iterations value.");
                return;
            }

            int size = Integer.parseInt(matrixSizeField.getText().trim());
            if (size < 2) {
                showAlert("Error", "Matrix size must be at least 2.");
                return;
            }
            if (size > 5) {
                showAlert("Warning", "Maximum matrix size is 5 for optimal display.");
                size = 5;
                matrixSizeField.setText("5");
            }

            currentSize = size;
            createMatrixInputFields(size);
            matrixInputPanel.setVisible(true);
            matrixInputPanel.setManaged(true);

        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid matrix size (2-5).");
        }
    }

    private void createMatrixInputFields(int size) {
        matrixGrid.getChildren().clear();
        matrixGrid.getRowConstraints().clear();
        matrixGrid.getColumnConstraints().clear();

        matrixFields = new TextField[size][size];
        rhsFields = new TextField[size];
        initialFields = new TextField[size];

        for (int j = 0; j < size; j++) {
            VBox headerBox = new VBox();
            headerBox.setAlignment(Pos.CENTER);
            headerBox.setSpacing(4);

            Label colLabel = new Label("x" + (j + 1));
            colLabel.getStyleClass().add("matrix-column-header");
            Label subLabel = new Label("column " + (j + 1));
            subLabel.getStyleClass().add("matrix-column-subheader");

            headerBox.getChildren().addAll(colLabel, subLabel);
            matrixGrid.add(headerBox, j + 1, 0);
        }

        VBox rhsHeader = new VBox();
        rhsHeader.setAlignment(Pos.CENTER);
        rhsHeader.setSpacing(4);
        Label rhsTitle = new Label("b");
        rhsTitle.getStyleClass().add("matrix-column-header-green");
        Label rhsSub = new Label("constants");
        rhsSub.getStyleClass().add("matrix-column-subheader");
        rhsHeader.getChildren().addAll(rhsTitle, rhsSub);
        matrixGrid.add(rhsHeader, size + 1, 0);

        VBox initialHeader = new VBox();
        initialHeader.setAlignment(Pos.CENTER);
        initialHeader.setSpacing(4);
        Label initialTitle = new Label("x₀");
        initialTitle.getStyleClass().add("matrix-column-header-orange");
        Label initialSub = new Label("initial guess");
        initialSub.getStyleClass().add("matrix-column-subheader");
        initialHeader.getChildren().addAll(initialTitle, initialSub);
        matrixGrid.add(initialHeader, size + 2, 0);

        for (int i = 0; i < size; i++) {
            HBox rowLabelBox = new HBox();
            rowLabelBox.setAlignment(Pos.CENTER_RIGHT);
            rowLabelBox.setSpacing(6);

            Label rowLabel = new Label("Eq " + (i + 1));
            rowLabel.getStyleClass().add("matrix-row-label");

            Label rowSub = new Label("row " + (i + 1));
            rowSub.getStyleClass().add("matri-row-sublabel");

            rowLabelBox.getChildren().addAll(rowLabel, rowSub);
            matrixGrid.add(rowLabelBox, 0, i + 1);

            for (int j = 0; j < size; j++) {
                TextField field = createTextField("matrix");
                field.setPromptText("a" + (i + 1) + (j + 1));

                matrixFields[i][j] = field;
                matrixGrid.add(field, j + 1, i + 1);
            }

            TextField rhsField = createTextField("rhs");
            rhsField.setPromptText("b" + (i + 1));
            rhsFields[i] = rhsField;
            matrixGrid.add(rhsField, size + 1, i + 1);

            TextField initialField = createTextField("initial");
            initialField.setPromptText("x₀" + (i + 1));
            initialFields[i] = initialField;
            matrixGrid.add(initialField, size + 2, i + 1);
        }

        // Add column constraints
        for (int i = 0; i <= size + 2; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            if (i == 0) {
                cc.setPrefWidth(80);
                cc.setMinWidth(80);
            } else {
                cc.setPrefWidth(110);
                cc.setMinWidth(100);
                cc.setHgrow(Priority.NEVER);
            }
            matrixGrid.getColumnConstraints().add(cc);
        }

        // Add row constraints
        for (int i = 0; i <= size; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setPrefHeight(65);
            rc.setMinHeight(60);
            matrixGrid.getRowConstraints().add(rc);
        }
    }

    private TextField createTextField(String type) {
        TextField field = new TextField();
        field.setPrefWidth(100);
        field.getStyleClass().add("matrix-input-field");

        String hoverColor = switch (type) {
            case "rhs" -> "#059669";
            case "initial" -> "#d97706";
            default -> "#4f46e5";
        };

        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(field.getStyle() + "-fx-border-color:" + hoverColor + "; -fx-border-width: 2;");
            } else {
                String currentStyle = field.getStyle();
                if (currentStyle.contains("-fx-border-color:" + hoverColor)) {
                    field.setStyle(currentStyle.replace("-fx-border-color:" + hoverColor + "; -fx-border-width: 2;", "-fx-border-color: #cbd5e1; -fx-border-width: 1;"));
                }
            }
        });

        return field;
    }

    private void solve() {
        try {
            SolverConfig currentConfig = getCurrentConfig();

            double tolerance = 1e-6;
            int maxIterations = 100;

            try {
                if (toleranceField != null && !toleranceField.getText().trim().isEmpty()) {
                    tolerance = Double.parseDouble(toleranceField.getText().trim());
                    if (tolerance <= 0) {
                        showAlert("Error", "Tolerance must be positive.");
                        return;
                    }
                }

                if (maxIterationsField != null && !maxIterationsField.getText().trim().isEmpty()) {
                    maxIterations = Integer.parseInt(maxIterationsField.getText().trim());
                    if (maxIterations <= 0) {
                        showAlert("Error", "Maximum iterations must be positive.");
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid tolerance or max iterations value.");
                return;
            }

            if (currentSize == 0) {
                showAlert("Error", "Please generate the matrix input fields first.");
                return;
            }

            // Parse matrix A
            double[][] A = new double[currentSize][currentSize];
            for (int i = 0; i < currentSize; i++) {
                for (int j = 0; j < currentSize; j++) {
                    String text = matrixFields[i][j].getText().trim();
                    if (text.isEmpty()) {
                        showAlert("Error", "Please fill all matrix coefficients.");
                        return;
                    }
                    A[i][j] = Double.parseDouble(text);
                }
            }

            // Parse vector b
            double[] b = new double[currentSize];
            for (int i = 0; i < currentSize; i++) {
                String text = rhsFields[i].getText().trim();
                if (text.isEmpty()) {
                    showAlert("Error", "Please fill all RHS values.");
                    return;
                }
                b[i] = Double.parseDouble(text);
            }

            // Parse initial values
            double[] initial = new double[currentSize];
            for (int i = 0; i < currentSize; i++) {
                String text = initialFields[i].getText().trim();
                if (text.isEmpty()) {
                    initial[i] = 0.0;
                } else {
                    initial[i] = Double.parseDouble(text);
                }
            }

            // Create and configure solver
            GaussJacobiSolver solver = new GaussJacobiSolver(A, b, initial, currentConfig);
            solver.setTolerance(tolerance);
            solver.setMaxIterations(maxIterations);

            GaussJacobiSolver.Result result = solver.solve();
            displayResults(result);

        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numeric values.");
        } catch (ArithmeticException e) {
            showAlert("Math Error", e.getMessage());
        } catch (IllegalArgumentException e) {
            showAlert("Error", e.getMessage());
        } catch (Exception e) {
            showAlert("Error", "An unexpected error occurred:\n" + e.getMessage());
        }
    }

    private void displayResults(GaussJacobiSolver.Result result) {
        resultCard.setVisible(true);
        resultCard.setManaged(true);

        StringBuilder solutionText = new StringBuilder();
        double[] solution = result.solution();
        for (int i = 0; i < solution.length; i++) {
            solutionText.append(String.format("x%d = ", i + 1))
                    .append(solution[i]);
            if (i < solution.length - 1) {
                solutionText.append("    ");
            }
        }
        solutionStatus.setText(solutionText.toString());
        solutionStatus.getStyleClass().add("text-success");

        if (result.converged()) {
            convergedStatus.setText("✓ Yes");
            convergedStatus.getStyleClass().add("text-success");
        } else {
            convergedStatus.setText("✗ No (max iterations reached)");
            convergedStatus.getStyleClass().add("text-error");
        }

        int iterCount = result.iterations().size();
        iterationsCount.setText(String.valueOf(iterCount));

        if (iterCount > 0) {
            double lastError = result.iterations().get(iterCount - 1).error();
            finalError.setText(String.valueOf(lastError));
        } else {
            finalError.setText("N/A");
        }

        tableCard.setVisible(true);
        tableCard.setManaged(true);
        populateTable(result);
    }

    private void populateTable(GaussJacobiSolver.Result result) {
        ObservableList<ObservableList<String>> tableData = FXCollections.observableArrayList();

        int solutionSize = result.solution().length;

        for (GaussJacobiSolver.Iteration iter : result.iterations()) {
            ObservableList<String> row = FXCollections.observableArrayList();
            row.add(String.valueOf(iter.iteration()));

            double[] xValues = iter.xValues();
            for (int i = 0; i < solutionSize; i++) {
                row.add(String.valueOf(xValues[i]));
            }

            for (int i = solutionSize; i < 5; i++) {
                row.add("—");
            }

            row.add(String.valueOf(iter.error()));
            tableData.add(row);
        }

        iterationsTable.setData(tableData);
    }

    private void clearAll() {
        matrixSizeField.setText("");
        matrixInputPanel.setVisible(false);
        matrixInputPanel.setManaged(false);

        if (toleranceField != null) {
            toleranceField.setText("1e-6");
        }
        if (maxIterationsField != null) {
            maxIterationsField.setText("100");
        }

        resultCard.setVisible(false);
        resultCard.setManaged(false);
        tableCard.setVisible(false);
        tableCard.setManaged(false);
        iterationsTable.clearData();

        currentSize = 0;
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