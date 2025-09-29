package com.example.cst8412ohly0001assignment2;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.net.URL;
import java.util.*;

public class DatasetController implements Initializable {

    @FXML public VBox rootPane;
    @FXML public ScrollPane viewPane;
    @FXML Menu recentFilesMenu;

    // Views
    private VBox addRecordView;
    private VBox singleRecordView;
    private List<TextField> fieldInputs = new ArrayList<>();
    int currentRowIndex = 0;

    private TableView<LinkedHashMap<String,String>> tableView;
    Pagination pagination;

    private VBox schemaEditorView;

    private String currentView = "table";

    /* ------------------------ Utility ------------------------ */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        refreshCurrentView();
    }

    void refreshCurrentView() {
        switch (currentView) {
            case "table" -> refreshTableView();
            case "single" -> refreshSingleRecord();
            case "add" -> refreshAddRecord();
            case "schema" -> showSchemaEditor();
        }
    }

    /* ------------------------ File selection ------------------------ */
    private FileChooser buildFileChooser(String title) {
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

        return fileChooser;
    }

    private File selectFileToOpen(String title) {
        FileChooser fileChooser = buildFileChooser(title);
        Window owner = rootPane.getScene().getWindow();
        return fileChooser.showOpenDialog(owner);
    }

    private File selectFileToSave(String title) {
        FileChooser fileChooser = buildFileChooser(title);
        Window owner = rootPane.getScene().getWindow();
        return fileChooser.showSaveDialog(owner);
    }

    /* ------------------------ File actions ------------------------ */
    @FXML private void newFile() {
        File file = selectFileToSave("New File");
        if (file != null) FileHandler.INSTANCE.readFile(file);
        showSchemaEditor();
    }

    @FXML private void openFile() {
        File file = selectFileToOpen("Open File");
        if (file != null) {
            FileHandler.INSTANCE.readFile(file);
            refreshCurrentView();
        }
    }

    @FXML private void saveFile() {
        FileHandler.INSTANCE.overwriteCurrentFile();
    }

    @FXML private void saveFileAs() {
        File file = selectFileToSave("Save As");
        if (file != null) {
            FileHandler.INSTANCE.writeFile(file);
            refreshCurrentView();
        }
    }

    @FXML private void closeFile() {
        // TODO add confirmation menu to save + close, not save + close, or cancel (keep open)
        FileHandler.INSTANCE.closeCurrentFile();
        refreshCurrentView();
    }

    @FXML private void revertFile() {
        FileHandler.INSTANCE.reloadCurrentFile();
        refreshCurrentView();
    }

    @FXML private void quit() { Platform.exit(); }

    @FXML
    private void showRecentFile() {
        recentFilesMenu.getItems().clear();

        LinkedHashSet<File> recentFiles = FileHandler.INSTANCE.getPreviousFiles();
        if (recentFiles.isEmpty()) {
            MenuItem none = new MenuItem("No recent files");
            none.setDisable(true);
            recentFilesMenu.getItems().add(none);
        } else {
            for (File file : recentFiles) {
                MenuItem item = new MenuItem(file.getName());
                item.setOnAction(_ -> FileHandler.INSTANCE.readFile(file));
                recentFilesMenu.getItems().add(item);
            }
        }
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
            LinkedHashMap<String,String> row = new LinkedHashMap<>();
            LinkedList<String> schema = FileHandler.INSTANCE.getSchema();

            for (int i = 0; i < schema.size(); i++) {
                row.put(schema.get(i), fieldInputs.get(i).getText());
            }

            // Perform AddRowCommand
            HistoryHandler.INSTANCE.perform(new AddRowCommand(
                    row,
                    FXCollections.observableArrayList(FileHandler.INSTANCE.getContents()),
                    this
            ), this);

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
        currentView = "add";
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
                LinkedHashMap<String,String> row = contents.get(currentRowIndex);

                // Perform RemoveRowCommand
                HistoryHandler.INSTANCE.perform(new RemoveRowCommand(
                        row,
                        currentRowIndex,
                        FXCollections.observableArrayList(contents),
                        this
                ), this);

                if (currentRowIndex >= contents.size())
                    currentRowIndex = contents.size() - 1;

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
                //refreshTableView();
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

            tf.focusedProperty().addListener((obs, oldFocused, isNowFocused) -> {
                if (!isNowFocused) { // user finished editing
                    String newValue = tf.getText();
                    String oldValue = row.get(key);
                    if (!newValue.equals(oldValue)) {
                        HistoryHandler.INSTANCE.perform(new EditCellCommand(row, key, newValue, this), this);
                        refreshTableView();
                    }
                }
            });

            grid.addRow(r++, label, tf);
        }

        singleRecordView.getChildren().add(grid);
    }

    @FXML public void showSingleRecordView() {
        initSingleRecordView();
        viewPane.setContent(singleRecordView);
        refreshSingleRecord();
        currentView = "single";
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

        LinkedList<String> headers = FileHandler.INSTANCE.getSchema();
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
        VBox page = createPage(0);
        viewPane.setContent(page);
    }

    private VBox createPage(int pageIndex) {
        LinkedList<LinkedHashMap<String,String>> contents = FileHandler.INSTANCE.getContents();
        int pageSize = 100;
        int fromIndex = pageIndex * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, contents.size());
        tableView.setItems(FXCollections.observableArrayList(contents.subList(fromIndex, toIndex)));
        return new VBox(tableView);
    }

    @FXML public void showGridView() {
        refreshTableView();
        currentView = "table";
    }

    /* ------------------------ Schema editor ------------------------ */
    @FXML
    private void showSchemaEditor() {
        if (schemaEditorView == null) {
            schemaEditorView = new VBox(10);
            schemaEditorView.setPadding(new Insets(10));
        } else {
            schemaEditorView.getChildren().clear();
        }

        // --- ListView for columns ---
        ListView<String> columnList = new ListView<>();
        columnList.getItems().addAll(FileHandler.INSTANCE.getSchema());
        columnList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        columnList.setPrefHeight(250);

        // --- Enable drag-and-drop reordering ---
        columnList.setCellFactory(_ -> {
            ListCell<String> cell = new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };

            cell.setOnDragDetected(event -> {
                if (!cell.isEmpty()) {
                    Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent cc = new ClipboardContent();
                    cc.putString(cell.getItem());
                    db.setContent(cc);
                    event.consume();
                }
            });

            cell.setOnDragOver(event -> {
                if (event.getGestureSource() != cell &&
                        event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            cell.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString()) {
                    int draggedIdx = columnList.getItems().indexOf(db.getString());
                    int thisIdx = cell.getIndex();
                    if (draggedIdx >= 0 && thisIdx >= 0 && draggedIdx != thisIdx) {
                        Collections.swap(columnList.getItems(), draggedIdx, thisIdx);

                        // Update FileHandler schema
                        FileHandler.INSTANCE.reorderColumns(columnList.getItems());

                        // Refresh UI views if necessary
                        refreshCurrentView();

                        // Update selection
                        columnList.getSelectionModel().clearAndSelect(thisIdx);
                    }
                    event.setDropCompleted(true);
                    event.consume();
                }
            });

            return cell;
        });

        // --- Buttons for adding/removing columns ---
        TextField columnName = new TextField();
        columnName.setPromptText("Column Name...");
        Button add = new Button("Add Column");
        Button remove = new Button("Remove Column");

        add.setMaxWidth(Double.MAX_VALUE);
        remove.setMaxWidth(Double.MAX_VALUE);

        add.setOnAction(_ -> {
            String name = columnName.getText();
            if (name != null && !name.isBlank() && !columnList.getItems().contains(name.strip())) {
                name = name.strip();

                // Perform AddColumnCommand
                HistoryHandler.INSTANCE.perform(new AddColumnCommand(
                        name,
                        FileHandler.INSTANCE.getContents(),
                        FileHandler.INSTANCE.getSchema(),
                        this
                ), this);

                columnList.getItems().add(name);
                refreshCurrentView();
            }
            columnName.clear();
        });

        remove.setOnAction(_ -> {
            String col = columnList.getSelectionModel().getSelectedItem();
            if (col != null) {
                // Perform RemoveColumnCommand
                HistoryHandler.INSTANCE.perform(new RemoveColumnCommand(
                        col,
                        FileHandler.INSTANCE.getContents(),
                        FileHandler.INSTANCE.getSchema(),
                        this
                ), this);

                columnList.getItems().remove(col);
                refreshCurrentView();
            }
        });

        VBox buttonBox = new VBox(5, columnName, add, remove);
        HBox editorBox = new HBox(10, columnList, buttonBox);

        // Handle empty schema
        if (columnList.getItems().isEmpty()) {
            schemaEditorView.getChildren().add(new Label("No schema found. Add columns to get started."));
        }

        schemaEditorView.getChildren().add(editorBox);

        viewPane.setContent(schemaEditorView);
        currentView = "schema";
    }

    @FXML
    private void undo() {
        HistoryHandler.INSTANCE.undo(this);
    }

    @FXML
    private void redo() {
        HistoryHandler.INSTANCE.redo(this);
    }
}