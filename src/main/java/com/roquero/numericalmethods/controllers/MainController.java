package com.roquero.numericalmethods.controllers;

import com.roquero.numericalmethods.math.SolverConfig;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

public class MainController {

    @FXML private ToggleGroup methods;
    @FXML private VBox workspace;

    @FXML private Spinner<Integer> decimalPlacesSpinner;

    @FXML private RadioButton roundDisplayOnlyRadio;
    @FXML private RadioButton roundEachIterationRadio;
    @FXML private ToggleGroup roundingModeGroup;

    private Object currentController;

    @FXML
    public void initialize() {
        // keep toggle selected; only switching to another button is allowed
        methods.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if (newToggle == null) {
                methods.selectToggle(oldToggle);
            }
        });

        decimalPlacesSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 15, 5)
        );

        setupSettingsListeners();

        openNewtonRaphson();
    }

    private void setupSettingsListeners() {
        decimalPlacesSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            syncSettingsToCurrentController();
        });

        roundingModeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            syncSettingsToCurrentController();
        });
    }

    private void syncSettingsToCurrentController() {
        if (currentController != null) {
            SolverConfig config = buildSolverConfig();

            // Add other controller types as you implement them
            // else if (currentController instanceof GaussJacobiController) {
            //     ((GaussJacobiController) currentController).setSolverConfig(config);
            // }
            // else if (currentController instanceof NewtonInterpolationController) {
            //     ((NewtonInterpolationController) currentController).setSolverConfig(config);
            // }
        }
    }

    public SolverConfig buildSolverConfig() {

        int decimals = decimalPlacesSpinner.getValue();

        boolean roundEachIteration =
                roundingModeGroup.getSelectedToggle() == roundEachIterationRadio;

        return new SolverConfig(decimals, roundEachIteration);
    }

    private void loadView(String fxmlName) {
        try {
            String path = "/" + fxmlName + ".fxml";

            System.out.println("Loading: " + path);

            var url = getClass().getResource(path);

            if (url == null) {
                throw new RuntimeException("FXML not found: " + path);
            }

            FXMLLoader loader = new FXMLLoader(url);
            Node view = loader.load();

            Object controller = loader.getController();
            currentController = controller;

            if (controller instanceof NewtonRaphsonController) {
                ((NewtonRaphsonController) controller).setMainController(this);
            }

            if (controller instanceof  SimpsonController) {
                ((SimpsonController) controller).setMainController(this);
            }

            if (controller instanceof  NewtonInterpolationController) {
                ((NewtonInterpolationController) controller).setMainController(this);
            }

            if (controller instanceof  GaussJacobiController) {
                ((GaussJacobiController) controller).setMainController(this);
            }

            if (controller instanceof  CentralDividedDifferenceController) {
                ((CentralDividedDifferenceController) controller).setMainController(this);
            }

            workspace.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openNewtonRaphson() {
        loadView("newton_raphson");
    }

    @FXML
    private void openGaussJacobi() {
        loadView("gauss_jacobi");
    }

    @FXML
    private void openNewtonInterpolation() {
        loadView("newton_interpolation");
    }

    @FXML
    private void openSimpson13() {
        loadView("simpson_13");
    }

    @FXML
    private void openCDD() {
        loadView("cdd");
    }

}
