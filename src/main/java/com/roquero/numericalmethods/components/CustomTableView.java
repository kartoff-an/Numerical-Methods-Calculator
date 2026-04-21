package com.roquero.numericalmethods.components;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

public class CustomTableView extends VBox {

    private final GridPane headerGrid;
    private final GridPane dataGrid;
    private final ScrollPane scrollPane;

    private final List<String> columnNames = new ArrayList<>();
    private final List<Double> columnWidths = new ArrayList<>();

    private ObservableList<ObservableList<String>> data;

    private final List<RowSelectionListener> selectionListeners = new ArrayList<>();

    private final List<HBox> rowNodes = new ArrayList<>();

    private int selectedRow = -1;

    public interface RowSelectionListener {
        void onRowSelected(int rowIndex, ObservableList<String> rowData);
    }

    public CustomTableView() {
        setSpacing(0);
        setStyle("""
            -fx-background-color: white;
            -fx-border-color: #e1e8ed;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
        """);

        // HEADER
        headerGrid = new GridPane();
        headerGrid.setAlignment(Pos.CENTER_LEFT);
        headerGrid.setStyle("""
            -fx-background-color: #f8f9fa;
            -fx-background-radius: 8 8 0 0;
            -fx-border-color: #e1e8ed;
            -fx-border-width: 0 0 1 0;
        """);

        // DATA GRID
        dataGrid = new GridPane();
        dataGrid.setAlignment(Pos.TOP_LEFT);

        scrollPane = new ScrollPane(dataGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("""
            -fx-background-color: white;
            -fx-background: white;
        """);

        VBox container = new VBox(headerGrid, scrollPane);
        container.setSpacing(0);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().add(container);
        setVgrow(container, Priority.ALWAYS);
    }

    // COLUMN SETUP
    public void addColumn(String name, double width) {
        int colIndex = columnNames.size();
        columnNames.add(name);
        columnWidths.add(width);

        Label header = new Label(name);
        header.setPrefWidth(width);
        header.setMinWidth(width);
        header.setMaxWidth(width);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("""
            -fx-font-size: 12px;
            -fx-font-weight: bold;
            -fx-text-fill: #2c3e50;
            -fx-padding: 10 12;
            -fx-background-color: #f8f9fa;
        """);

        headerGrid.add(header, colIndex, 0);

        ColumnConstraints cc = new ColumnConstraints();
        cc.setPrefWidth(width);
        cc.setMinWidth(width);
        cc.setMaxWidth(width);
        cc.setHgrow(Priority.NEVER);

        headerGrid.getColumnConstraints().add(cc);
        dataGrid.getColumnConstraints().add(cc);
    }

    // DATA
    public void setData(ObservableList<ObservableList<String>> data) {
        this.data = data;
        refreshTable();
    }

    public void clearData() {
        dataGrid.getChildren().clear();
        dataGrid.getRowConstraints().clear();
        rowNodes.clear();
        selectedRow = -1;
    }

    // RENDER
    public void refreshTable() {
        dataGrid.getChildren().clear();
        dataGrid.getRowConstraints().clear();
        rowNodes.clear();

        // Reset the scroll position
        scrollPane.setVvalue(0);

        if (data == null || data.isEmpty()) {
            // Show empty message
            Label emptyLabel = new Label("No data to display");
            emptyLabel.setStyle("""
                -fx-font-size: 12px;
                -fx-text-fill: #9ca3af;
                -fx-padding: 20;
            """);
            emptyLabel.setAlignment(Pos.CENTER);
            dataGrid.add(emptyLabel, 0, 0);
            return;
        }

        for (int i = 0; i < data.size(); i++) {
            ObservableList<String> row = data.get(i);
            final int rowIndex = i;

            HBox rowBox = new HBox();
            rowBox.setStyle("-fx-background-color: white;");
            rowBox.setPrefHeight(36);
            rowBox.setMinHeight(36);
            rowBox.setMaxHeight(36);
            rowBox.setAlignment(Pos.CENTER_LEFT);

            // Alternating row colors
            if (i % 2 == 1) {
                rowBox.setStyle("-fx-background-color: #fafafa;");
            }

            for (int j = 0; j < columnNames.size(); j++) {
                Label cell = getLabel(j, row);

                rowBox.getChildren().add(cell);
            }

            // Store original style for hover/selection
            final String originalStyle = rowBox.getStyle();

            // Hover behavior
            rowBox.setOnMouseEntered(e -> {
                if (selectedRow != rowIndex) {
                    rowBox.setStyle(originalStyle + "-fx-background-color: #f8f9fa;");
                }
            });

            rowBox.setOnMouseExited(e -> {
                if (selectedRow != rowIndex) {
                    rowBox.setStyle(originalStyle);
                }
            });

            rowBox.setOnMouseClicked(e -> setSelectedRow(rowIndex));

            rowNodes.add(rowBox);
            dataGrid.add(rowBox, 0, i);

            RowConstraints rc = new RowConstraints();
            rc.setPrefHeight(36);
            rc.setMinHeight(36);
            rc.setMaxHeight(36);
            dataGrid.getRowConstraints().add(rc);
        }
    }

    private Label getLabel(int j, ObservableList<String> row) {
        String value = (j < row.size()) ? row.get(j) : "";

        Label cell = new Label(value);
        cell.setPrefWidth(columnWidths.get(j));
        cell.setMinWidth(columnWidths.get(j));
        cell.setMaxWidth(columnWidths.get(j));
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setWrapText(false);

        // Truncate long text
        if (value.length() > 50) {
            cell.setText(value.substring(0, 47) + "...");
        }

        cell.setStyle("""
            -fx-font-size: 12px;
            -fx-text-fill: #1f2937;
            -fx-padding: 8 12;
            -fx-font-family: 'Courier New', monospace;
        """);

        // Add right border except for last column
        if (j < columnNames.size() - 1) {
            cell.setStyle(cell.getStyle() +
                    "-fx-border-color: #f0f3f6; -fx-border-width: 0 1 0 0;");
        }
        return cell;
    }

    // SELECTION
    public void setSelectedRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= rowNodes.size()) return;

        // Clear previous selection
        if (selectedRow >= 0 && selectedRow < rowNodes.size()) {
            HBox previousRow = rowNodes.get(selectedRow);
            if (selectedRow % 2 == 1) {
                previousRow.setStyle("-fx-background-color: #fafafa;");
            } else {
                previousRow.setStyle("-fx-background-color: white;");
            }
        }

        selectedRow = rowIndex;

        // Apply new selection
        HBox selected = rowNodes.get(selectedRow);
        selected.setStyle("-fx-background-color: #dbeafe;");

        // Notify listeners
        if (data != null && selectedRow < data.size()) {
            ObservableList<String> rowData = data.get(selectedRow);
            for (RowSelectionListener listener : selectionListeners) {
                listener.onRowSelected(selectedRow, rowData);
            }
        }
    }

    public int getSelectedRow() {
        return selectedRow;
    }

    public void addRowSelectionListener(RowSelectionListener listener) {
        selectionListeners.add(listener);
    }

    public void removeAllRows() {
        clearData();
        refreshTable();
    }
}