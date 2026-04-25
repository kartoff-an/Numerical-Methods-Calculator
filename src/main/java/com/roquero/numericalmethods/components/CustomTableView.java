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
            -fx-border-radius: 8;
            -fx-background-radius: 8;
        """);

        headerGrid = new GridPane();
        headerGrid.setAlignment(Pos.CENTER);
        headerGrid.getStyleClass().add("column-header");

        dataGrid = new GridPane();
        dataGrid.setAlignment(Pos.TOP_LEFT);
        dataGrid.getStyleClass().add("row-container");

        scrollPane = new ScrollPane(dataGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("scroll-bar");

        VBox container = new VBox(headerGrid, scrollPane);
        container.setSpacing(0);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().add(container);
        setVgrow(container, Priority.ALWAYS);
    }

    public void addColumn(String name, double width) {
        int colIndex = columnNames.size();
        columnNames.add(name);
        columnWidths.add(width);

        Label header = new Label(name);
        header.setPrefWidth(width);
        header.setMinWidth(width);
        header.setMaxWidth(width);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("column-header");

        headerGrid.add(header, colIndex, 0);

        ColumnConstraints cc = new ColumnConstraints();
        cc.setPrefWidth(width);
        cc.setMinWidth(width);
        cc.setMaxWidth(width);
        cc.setHgrow(Priority.NEVER);

        headerGrid.getColumnConstraints().add(cc);
        dataGrid.getColumnConstraints().add(cc);
    }

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

    public void refreshTable() {
        dataGrid.getChildren().clear();
        dataGrid.getRowConstraints().clear();
        rowNodes.clear();

        scrollPane.setVvalue(0);

        if (data == null || data.isEmpty()) {
            Label emptyLabel = new Label("No data to display");
            emptyLabel.getStyleClass().add("empty-message");
            emptyLabel.setAlignment(Pos.CENTER);
            dataGrid.add(emptyLabel, 0, 0);
            return;
        }

        for (int i = 0; i < data.size(); i++) {
            ObservableList<String> row = data.get(i);
            final int rowIndex = i;

            HBox rowBox = new HBox();
            rowBox.getStyleClass().add("table-row");
            rowBox.setPrefHeight(36);
            rowBox.setMinHeight(36);
            rowBox.setMaxHeight(36);
            rowBox.setAlignment(Pos.CENTER_LEFT);

            if (i % 2 == 1) {
                rowBox.setStyle("-fx-background-color: #fafafa;");
            }

            for (int j = 0; j < columnNames.size(); j++) {
                Label cell = getLabel(j, row);

                rowBox.getChildren().add(cell);
            }

            final String originalStyle = rowBox.getStyle();

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

        if (value.length() > 50) {
            cell.setText(value.substring(0, 47) + "...");
        }

        cell.getStyleClass().add("table-cell");

        if (j < columnNames.size() - 1) {
            cell.setStyle(cell.getStyle() +
                    "-fx-border-color: #f0f3f6; -fx-border-width: 0 1 0 0;");
        }
        return cell;
    }

    public void setSelectedRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= rowNodes.size()) return;

        if (selectedRow >= 0 && selectedRow < rowNodes.size()) {
            HBox previousRow = rowNodes.get(selectedRow);
            if (selectedRow % 2 == 1) {
                previousRow.setStyle("-fx-background-color: #fafafa;");
            } else {
                previousRow.setStyle("-fx-background-color: white;");
            }
        }

        selectedRow = rowIndex;

        HBox selected = rowNodes.get(selectedRow);
        selected.setStyle("-fx-background-color: #dbeafe;");

        if (data != null && selectedRow < data.size()) {
            ObservableList<String> rowData = data.get(selectedRow);
            for (RowSelectionListener listener : selectionListeners) {
                listener.onRowSelected(selectedRow, rowData);
            }
        }
    }
}