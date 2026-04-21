module com.roquero.numericalmethods {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires javafx.swing;
    requires exp4j;

    opens com.roquero.numericalmethods to javafx.fxml;
    opens com.roquero.numericalmethods.controllers to javafx.fxml;

    exports com.roquero.numericalmethods;
    exports com.roquero.numericalmethods.components;
}