package com.roquero.numericalmethods;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader root = new FXMLLoader(
                    getClass().getResource("/main.fxml")
            );

            Scene scene = new Scene(root.load());
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/app.css")).toExternalForm()
            );
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/custom-table.css")).toExternalForm()
            );

            // Set application icon/logo
            setAppIcon(stage);

            stage.setTitle("Numerical Methods Calculator");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAppIcon(Stage stage) {
        try {
            String logoPath = "/images/icon.png";

            Image appIcon = null;
            var url = getClass().getResource(logoPath);
            if (url != null) {
                appIcon = new Image(url.toExternalForm());
            }

            if (appIcon != null) {
                stage.getIcons().add(appIcon);
            } else {
                System.out.println("Logo not found, using default JavaFX icon");
            }
        } catch (Exception e) {
            System.out.println("Could not load app icon: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}