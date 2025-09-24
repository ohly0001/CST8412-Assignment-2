package com.example.cst8412ohly0001assignment2;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.*;

public class DatasetController {

    @FXML public VBox rootPane;
    @FXML public ScrollPane viewPane;
    @FXML Menu recentFilesMenu;

    // Views
    private VBox addRecordView;
    private VBox singleRecordView;
    private List<TextField> fieldInputs = new ArrayList<>();
    private int currentRowIndex = 0;

    private TableView<LinkedHashMap<String,String>> tableView;
    private Pagination pagination;

    private VBox schemaEditorView;

    /* ------------------------ File selection ------------------------ */
    private File selectFile(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);

        File lastFile = FileHandler.INSTANCE.getCurrentFile();
        File initialDir = (lastFile != null && lastFile.getParentFile() != null)
                ? lastFile.getParentFile()
                : new File(System.getProperty("user.home"));
        if (initialDir.exists()) fileChooser.setInitialDirectory(initialDir);

        FileChooser.ExtensionFilter allSafe = new FileChooser.ExtensionFilter("All Supported", "*.csv", "*.json", "*.yaml", "*.xml");
        FileChooser.ExtensionFilter csv = new FileChooser.ExtensionFilter("CSV Files", "*.csv");
        FileChooser.ExtensionFilter json = new FileChooser.ExtensionFilter("JSON Files", "*.json");
        FileChooser.ExtensionFilter yaml = new FileChooser.ExtensionFilter("YAML Files", "*.yaml");
        FileChooser.ExtensionFilter xml = new FileChooser.ExtensionFilter("XML Files", "*.xml");
        FileChooser.ExtensionFilter all = new FileChooser.ExtensionFilter("All Files", "*.*");

        fileChooser.getExtensionFilters().addAll(allSafe, csv, json, yaml, xml, all);

        String ext = lastFile != null ? FileHandler.INSTANCE.getFileExtension(lastFile) : "";
        fileChooser.setSelectedExtensionFilter(switch (ext) {
            case "csv" -> csv;
            case "json" -> json;
            case "yaml" -> yaml;
            case "xml" -> xml;
            default -> allSafe;
        });

        Window owner = rootPane.getScene().getWindow();
        return fileChooser.showOpenDialog(owner);
    }

    /* ------------------------ File actions ------------------------ */
    @FXML private void newFile() { File file = selectFile("New File"); if (file != null) FileHandler.INSTANCE.readFile(file); }
    @FXML private void openFile() { File file = selectFile("Open File"); if (file != null) FileHandler.INSTANCE.readFile(file); }
    @FXML private void saveFile() { FileHandler.INSTANCE.overwriteCurrentFile(); }
    @FXML private void saveFileAs() { File file = selectFile("Save As"); if (file != null) FileHandler.INSTANCE.readFile(file); }
    @FXML private void revertFile() { FileHandler.INSTANCE.reloadCurrentFile(); }
    @FXML private void quit() { Platform.exit(); }

    @FXML private void showRecentFile() {
        recentFilesMenu.setOnShowing(_ -> {
            recentFilesMenu.getItems().clear();
            FileHandler.INSTANCE.getPreviousFiles().forEach(file -> {
                MenuItem item = new MenuItem(file.getName());
                item.setOnAction(_ -> FileHandler.INSTANCE.readFile(file));
                recentFilesMenu.getItems().add(item);
            });
        });
    }

    /* ------------------------ Add record view ------------------------ */
    private void initAddRecordView() {
        if (addRecordView != null) return;

        addRecordView = new VBox(5);
        addRecordView.setPadding(new Insets(10));
        viewPane.setContent(addRecordView);

        fieldInputs = new ArrayList<>();
        Button saveButton = new Button("Save");
        Button clearButton = new Button("Clear");
        HBox navBox = new HBox(5, saveButton, clearButton);
        addRecordView.getChildren().add(navBox);

        saveButton.setOnAction(_ -> {
            LinkedList<LinkedHashMap<String,String>> contents = FileHandler.INSTANCE.getContents();
            LinkedHashMap<String,String> row = new LinkedHashMap<>();
            LinkedList<String> schema = FileHandler.INSTANCE.getSchema();

            for (int i = 0; i < schema.size(); i++) {
                row.put(schema.get(i), fieldInputs.get(i).getText());
            }

            contents.addLast(row);
            refreshTableView();
            refreshAddRecord();
        });

        clearButton.setOnAction(_ -> fieldInputs.forEach(TextInputControl::clear));
    }

    private void refreshAddRecord() {
        if (addRecordView == null) initAddRecordView();
        addRecordView.getChildren().removeIf(node -> node instanceof GridPane);
        fieldInputs.clear();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);

        LinkedList<String> schema = FileHandler.INSTANCE.getSchema();
        for (int r = 0; r < schema.size(); r++) {
            Label label = new Label(schema.get(r));
            TextField tf = new TextField();
            fieldInputs.add(tf);
            grid.addRow(r, label, tf);
        }

        addRecordView.getChildren().add(grid);
    }

    @FXML public void showAddRecordView() {
        initAddRecordView();
        viewPane.setContent(addRecordView);
        refreshAddRecord();
    }

    /* ------------------------ Single-record view ------------------------ */
    private void initSingleRecordView() {
        if (singleRecordView != null) return;

        singleRecordView = new VBox(5);
        singleRecordView.setPadding(new Insets(10));
        viewPane.setContent(singleRecordView);

        fieldInputs = new ArrayList<>();
        Button prevButton = new Button("<");
        Button nextButton = new Button(">");
        Button deleteButton = new Button("Delete");
        Button saveButton = new Button("Save");
        HBox navBox = new HBox(5, prevButton, nextButton, deleteButton, saveButton);
        singleRecordView.getChildren().add(navBox);

        prevButton.setOnAction(_ -> { if (currentRowIndex > 0) currentRowIndex--; refreshSingleRecord(); });
        nextButton.setOnAction(_ -> { if (currentRowIndex < FileHandler.INSTANCE.getContents().size() - 1) currentRowIndex++; refreshSingleRecord(); });
        deleteButton.setOnAction(_ -> {
            LinkedList<LinkedHashMap<String,String>> contents = FileHandler.INSTANCE.getContents();
            if (!contents.isEmpty()) {
                contents.remove(currentRowIndex);
                if (currentRowIndex >= contents.size()) currentRowIndex = contents.size() - 1;
                refreshSingleRecord();
                refreshTableView();
            }
        });
        saveButton.setOnAction(_ -> {
            LinkedList<LinkedHashMap<String,String>> contents = FileHandler.INSTANCE.getContents();
            if (!contents.isEmpty()) {
                LinkedHashMap<String,String> row = contents.get(currentRowIndex);
                List<String> keys = new ArrayList<>(row.keySet());
                for (int i = 0; i < fieldInputs.size(); i++) {
                    row.put(keys.get(i), fieldInputs.get(i).getText());
                }
                refreshTableView();
            }
        });
    }

    private void refreshSingleRecord() {
        if (singleRecordView == null) initSingleRecordView();
        singleRecordView.getChildren().removeIf(node -> node instanceof GridPane);
        fieldInputs.clear();

        LinkedList<LinkedHashMap<String,String>> contents = FileHandler.INSTANCE.getContents();
        if (contents.isEmpty()) return;

        LinkedHashMap<String,String> row = contents.get(currentRowIndex);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);

        int r = 0;
        for (String key : row.keySet()) {
            Label label = new Label(key);
            TextField tf = new TextField(row.get(key));
            fieldInputs.add(tf);
            grid.addRow(r++, label, tf);
        }

        singleRecordView.getChildren().add(grid);
    }

    @FXML public void showSingleRecordView() {
        initSingleRecordView();
        viewPane.setContent(singleRecordView);
        refreshSingleRecord();
    }

    /* ------------------------ Grid view ------------------------ */
    private void initGridView() {
        if (tableView != null) return;

        tableView = new TableView<>();
        pagination = new Pagination();
        pagination.setPageFactory(this::createPage);

        VBox tableContainer = new VBox(5, tableView, pagination);
        tableContainer.setPadding(new Insets(5));
        viewPane.setContent(tableContainer);
    }

    private void refreshTableView() {
        LinkedList<LinkedHashMap<String,String>> contents = FileHandler.INSTANCE.getContents();
        if (contents.isEmpty()) {
            viewPane.setContent(new Label("No data available."));
            return;
        }

        initGridView();

        Set<String> headers = contents.getFirst().keySet();
        if (tableView.getColumns().isEmpty() || tableView.getColumns().size() != headers.size()) {
            tableView.getColumns().clear();
            for (String header : headers) {
                TableColumn<LinkedHashMap<String,String>, String> col = new TableColumn<>(header);
                col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getOrDefault(header, "")));
                tableView.getColumns().add(col);
            }
        }

        int pageCount = (int) Math.ceil(contents.size() / 100.0);
        pagination.setPageCount(Math.max(pageCount, 1));
        pagination.setCurrentPageIndex(0);
        createPage(0);
    }

    private VBox createPage(int pageIndex) {
        LinkedList<LinkedHashMap<String,String>> contents = FileHandler.INSTANCE.getContents();
        int pageSize = 100;
        int fromIndex = pageIndex * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, contents.size());
        tableView.setItems(FXCollections.observableArrayList(contents.subList(fromIndex, toIndex)));
        return new VBox(tableView);
    }

    @FXML public void showGridView() { refreshTableView(); }

    /* ------------------------ Schema editor ------------------------ */
    @FXML private void showSchemaEditor() {
        if (schemaEditorView == null) {
            schemaEditorView = new VBox(10);
            schemaEditorView.setPadding(new Insets(10));
        } else {
            schemaEditorView.getChildren().clear();
        }

        var contents = FileHandler.INSTANCE.getContents();
        if (contents.isEmpty()) {
            schemaEditorView.getChildren().add(new Label("No data loaded."));
        } else {
            var headers = contents.getFirst().keySet();
            headers.forEach(h -> schemaEditorView.getChildren().add(new Label("Column: " + h)));

            Button addCol = new Button("Add Column");
            Button removeCol = new Button("Remove Column");
            schemaEditorView.getChildren().addAll(addCol, removeCol);

            // TODO: hook up schema change actions
        }

        viewPane.setContent(schemaEditorView);
    }
}