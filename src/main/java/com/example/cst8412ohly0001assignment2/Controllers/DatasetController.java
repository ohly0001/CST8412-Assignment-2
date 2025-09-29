package com.example.cst8412ohly0001assignment2.Controllers;

import com.example.cst8412ohly0001assignment2.Commands.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private int currentRowIndex = 0;

    private TableView<LinkedHashMap<String,String>> tableView;
    private Pagination pagination;

    private VBox schemaEditorView;

    private String currentView = "table";
    private Stage stage = null;

    private final int rowsPerPage = 100;

    private SortedList<LinkedHashMap<String, String>> sortedContents;

    /* ------------------------ Initialization ------------------------ */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Platform.runLater(() -> {
            stage = (Stage) rootPane.getScene().getWindow();
            stage.setOnCloseRequest(e -> {
                if (!quit()) e.consume(); // prevent closing if user cancels
            });
        });
        refreshCurrentView();
    }

    public void refreshCurrentView() {
        switch (currentView) {
            case "table" -> refreshTableView();
            case "single" -> refreshSingleRecord();
            case "add" -> refreshAddRecord();
            case "schema" -> showSchemaEditor();
        }
    }

    /* ------------------------ Grid view ------------------------ */
    private void initGridView() {
        if (tableView != null) return;

        tableView = new TableView<>();
        pagination = new Pagination();

        LinkedList<LinkedHashMap<String,String>> rawContents = FileHandler.INSTANCE.getContents();
        ObservableList<LinkedHashMap<String, String>> observableContents = FXCollections.observableArrayList(rawContents);

        sortedContents = new SortedList<>(observableContents);
        tableView.setItems(sortedContents);
        sortedContents.comparatorProperty().bind(tableView.comparatorProperty());

        pagination.setPageFactory(this::createPage);

        // Row click factory
        tableView.setRowFactory(_ -> {
            TableRow<LinkedHashMap<String,String>> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    int rowIndexOnPage = row.getIndex();
                    int pageIndex = pagination.getCurrentPageIndex();
                    currentRowIndex = pageIndex * rowsPerPage + rowIndexOnPage;
                    showSingleRecordView();
                }
            });
            return row;
        });
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

        int pageCount = Math.ceilDiv(contents.size(), rowsPerPage);
        pagination.setPageCount(Math.max(pageCount, 1));
        pagination.setCurrentPageIndex(0);
    }

    private VBox createPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, sortedContents.size());

        // Use a sublist view for the page
        tableView.setItems(FXCollections.observableArrayList(sortedContents.subList(fromIndex, toIndex)));

        // Highlight selected row if it belongs to this page
        int relativeIndex = currentRowIndex - (pageIndex * rowsPerPage);
        if (relativeIndex >= 0 && relativeIndex < tableView.getItems().size()) {
            tableView.getSelectionModel().select(relativeIndex);
            tableView.scrollTo(relativeIndex);
        }

        return new VBox(tableView);
    }

    @FXML
    public void showGridView() {
        refreshTableView();  // updates columns and pagination
        VBox tableContainer = new VBox(5, tableView, pagination);
        tableContainer.setPadding(new Insets(5));
        viewPane.setContent(tableContainer);  // re-attach every time
        currentView = "table";
    }

    /* ------------------------ Single-record view ------------------------ */
    private void initSingleRecordView() {
        if (singleRecordView != null) return;

        singleRecordView = new VBox(5);
        singleRecordView.setPadding(new Insets(10));
        viewPane.setContent(singleRecordView);

        fieldInputs = new ArrayList<>();
        Button firstButton = new Button("<<");
        Button prevButton = new Button("<");
        Button nextButton = new Button(">");
        Button lastButton = new Button(">>");
        Button deleteButton = new Button("Delete");
        Button saveButton = new Button("Save");
        HBox navBox = new HBox(5, firstButton, prevButton, nextButton, lastButton, deleteButton, saveButton);
        singleRecordView.getChildren().add(navBox);

        firstButton.setOnAction(_ -> { currentRowIndex = 0; refreshSingleRecord(); });
        prevButton.setOnAction(_ -> {
            LinkedList<LinkedHashMap<String,String>> contents = FileHandler.INSTANCE.getContents();
            currentRowIndex = currentRowIndex > 0 ? currentRowIndex - 1 : contents.size() - 1;
            refreshSingleRecord();
        });
        nextButton.setOnAction(_ -> {
            LinkedList<LinkedHashMap<String,String>> contents = FileHandler.INSTANCE.getContents();
            currentRowIndex = currentRowIndex < contents.size() - 1 ? currentRowIndex + 1 : 0;
            refreshSingleRecord();
        });
        lastButton.setOnAction(_ -> {
            LinkedList<LinkedHashMap<String,String>> contents = FileHandler.INSTANCE.getContents();
            currentRowIndex = contents.size() - 1;
            refreshSingleRecord();
        });
        deleteButton.setOnAction(_ -> {
            LinkedList<LinkedHashMap<String,String>> contents = FileHandler.INSTANCE.getContents();
            if (!contents.isEmpty()) {
                LinkedHashMap<String,String> row = contents.get(currentRowIndex);
                int oldRowIndex = currentRowIndex;
                HistoryHandler.INSTANCE.perform(new RemoveRowCommand(oldRowIndex, row, this));
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

            tf.focusedProperty().addListener((_, _, isNowFocused) -> {
                if (!isNowFocused) {
                    String newValue = tf.getText();
                    String oldValue = row.get(key);
                    if (!newValue.equals(oldValue)) {
                        HistoryHandler.INSTANCE.perform(new EditCellCommand(row, key, newValue, this));
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

    /* ------------------------ Accessors ------------------------ */
    public int getCurrentRowIndex() { return currentRowIndex; }
    public String getCurrentView() { return currentView; }
    public void setCurrentRowIndex(int rowIndex) { this.currentRowIndex = rowIndex; }
    public void setCurrentView(String newView) { this.currentView = newView; }
    public int getCurrentPageIndex() { return pagination != null ? pagination.getCurrentPageIndex() : 0; }
    public void setCurrentPageIndex(int paginationIndex) { if (pagination != null) pagination.setCurrentPageIndex(paginationIndex); }

    /* ------------------------ About Dialog ------------------------ */
    @FXML
    private void showAboutView() {
        Alert aboutDialog = new Alert(Alert.AlertType.INFORMATION);
        aboutDialog.setTitle("About");
        aboutDialog.setHeaderText("This application edits and views CSV, JSON, XML, and YAML files.");
        aboutDialog.setContentText("ohly0001 CST8412 Assignment 2\nJavaFX API Version 24.0.1");
        aboutDialog.showAndWait();
    }

    /* ------------------------ Add record view ------------------------ */
    private void initAddRecordView() {
        if (addRecordView != null) return;

        addRecordView = new VBox(10);
        addRecordView.setPadding(new Insets(10));

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

            int oldRowIndex = currentRowIndex;
            HistoryHandler.INSTANCE.perform(new AddRowCommand(oldRowIndex, row, this));

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
        grid.setHgap(15);
        grid.setVgap(10);

        LinkedList<String> schema = FileHandler.INSTANCE.getSchema();
        for (int r = 0; r < schema.size(); r++) {
            Label label = new Label(schema.get(r));
            TextField tf = new TextField();
            fieldInputs.add(tf);
            grid.addRow(r, label, tf);
        }

        addRecordView.getChildren().add(grid);
    }

    @FXML
    public void showAddRecordView() {
        initAddRecordView();
        refreshAddRecord();
        viewPane.setContent(addRecordView);
        currentView = "add";
    }

    /* ------------------------ Schema editor ------------------------ */
    @FXML
    public void showSchemaEditor() {
        if (schemaEditorView == null) {
            schemaEditorView = new VBox(10);
            schemaEditorView.setPadding(new Insets(10));
        } else {
            schemaEditorView.getChildren().clear();
        }

        ListView<String> columnList = new ListView<>();
        columnList.getItems().addAll(FileHandler.INSTANCE.getSchema());
        columnList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        columnList.setPrefHeight(250);

        // Drag-and-drop reordering
        columnList.setCellFactory(_ -> {
            ListCell<String> cell = new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };

            cell.setOnDragDetected(e -> {
                if (!cell.isEmpty()) {
                    var db = cell.startDragAndDrop(TransferMode.MOVE);
                    var cc = new javafx.scene.input.ClipboardContent();
                    cc.putString(cell.getItem());
                    db.setContent(cc);
                    e.consume();
                }
            });

            cell.setOnDragOver(e -> {
                if (e.getGestureSource() != cell && e.getDragboard().hasString()) {
                    e.acceptTransferModes(TransferMode.MOVE);
                }
                e.consume();
            });

            cell.setOnDragDropped(e -> {
                var db = e.getDragboard();
                if (db.hasString()) {
                    int draggedIdx = columnList.getItems().indexOf(db.getString());
                    int thisIdx = cell.getIndex();
                    if (draggedIdx >= 0 && thisIdx >= 0 && draggedIdx != thisIdx) {
                        Collections.swap(columnList.getItems(), draggedIdx, thisIdx);
                        FileHandler.INSTANCE.reorderColumns(columnList.getItems());
                        refreshCurrentView();
                        columnList.getSelectionModel().clearAndSelect(thisIdx);
                    }
                    e.setDropCompleted(true);
                    e.consume();
                }
            });

            return cell;
        });

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
                HistoryHandler.INSTANCE.perform(new AddColumnCommand(name,
                        FileHandler.INSTANCE.getContents(),
                        FileHandler.INSTANCE.getSchema(),
                        this));
                columnList.getItems().add(name);
                refreshCurrentView();
            }
            columnName.clear();
        });

        remove.setOnAction(_ -> {
            String col = columnList.getSelectionModel().getSelectedItem();
            if (col != null) {
                HistoryHandler.INSTANCE.perform(new RemoveColumnCommand(col,
                        FileHandler.INSTANCE.getContents(),
                        FileHandler.INSTANCE.getSchema(),
                        this));
                columnList.getItems().remove(col);
                refreshCurrentView();
            }
        });

        VBox buttonBox = new VBox(5, columnName, add, remove);
        HBox editorBox = new HBox(10, columnList, buttonBox);

        if (columnList.getItems().isEmpty()) {
            schemaEditorView.getChildren().add(new Label("No schema found. Add columns to get started."));
        }

        schemaEditorView.getChildren().add(editorBox);
        viewPane.setContent(schemaEditorView);
        currentView = "schema";
    }

    /* ------------------------ File Selection ------------------------ */
    private FileChooser buildFileChooser(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);

        // Use last opened file's directory or default to home
        File lastFile = FileHandler.INSTANCE.getCurrentFile();
        File initialDir = (lastFile != null && lastFile.getParentFile() != null)
                ? lastFile.getParentFile()
                : new File(System.getProperty("user.home"));
        if (initialDir.exists()) fileChooser.setInitialDirectory(initialDir);

        // Extension filters
        FileChooser.ExtensionFilter allSafe = new FileChooser.ExtensionFilter(
                "All Supported", "*.csv", "*.json", "*.yaml", "*.xml");
        FileChooser.ExtensionFilter csv = new FileChooser.ExtensionFilter("CSV Files", "*.csv");
        FileChooser.ExtensionFilter json = new FileChooser.ExtensionFilter("JSON Files", "*.json");
        FileChooser.ExtensionFilter yaml = new FileChooser.ExtensionFilter("YAML Files", "*.yaml");
        FileChooser.ExtensionFilter xml = new FileChooser.ExtensionFilter("XML Files", "*.xml");
        FileChooser.ExtensionFilter all = new FileChooser.ExtensionFilter("All Files", "*.*");

        fileChooser.getExtensionFilters().addAll(allSafe, csv, json, yaml, xml, all);

        // Preselect filter based on last file
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

    @FXML private void newFile() {
        File file = selectFileToSave("New File");
        if (file != null) {
            FileHandler.INSTANCE.readFile(file);
            showSchemaEditor();
        }
    }

    @FXML private void openFile() {
        File file = selectFileToOpen("Open File");
        if (file != null) {
            HistoryHandler.INSTANCE.perform(new OpenFileCommand(file, this));
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

    @FXML
    private boolean quit() {
        AtomicBoolean canClose = new AtomicBoolean(false);

        if (FileHandler.INSTANCE.getCurrentFile() != null) {
            ButtonType saveQuit = new ButtonType("Save & Quit", ButtonBar.ButtonData.YES);
            ButtonType quitNoSave = new ButtonType("Quit Without Saving", ButtonBar.ButtonData.NO);
            ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            Alert quitAlert = new Alert(Alert.AlertType.CONFIRMATION);
            quitAlert.setTitle("Quit Application");
            quitAlert.setHeaderText("Are you sure you want to quit?");
            quitAlert.setContentText("Choose an option:");
            quitAlert.getButtonTypes().setAll(saveQuit, quitNoSave, cancel);

            // Styling buttons
            quitAlert.getDialogPane().lookupButton(saveQuit).setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            quitAlert.getDialogPane().lookupButton(quitNoSave).setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            quitAlert.getDialogPane().lookupButton(cancel).setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white;");

            quitAlert.showAndWait().ifPresent(result -> {
                if (result == saveQuit) {
                    FileHandler.INSTANCE.overwriteCurrentFile(); // save logic
                    canClose.set(true);
                } else if (result == quitNoSave) {
                    canClose.set(true);
                }
                // Cancel -> do nothing
            });

        } else {
            ButtonType quit = new ButtonType("Quit", ButtonBar.ButtonData.YES);
            ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            Alert quitAlert = new Alert(Alert.AlertType.CONFIRMATION);
            quitAlert.setTitle("Quit Application");
            quitAlert.setHeaderText("Are you sure you want to quit?");
            quitAlert.setContentText("Choose an option:");
            quitAlert.getButtonTypes().setAll(quit, cancel);

            // Styling buttons
            quitAlert.getDialogPane().lookupButton(quit).setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            quitAlert.getDialogPane().lookupButton(cancel).setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white;");

            quitAlert.showAndWait().ifPresent(result -> {
                if (result == quit) {
                    canClose.set(true);
                }
                // Cancel -> do nothing
            });
        }

        if (canClose.get()) {
            Platform.exit();
        }

        return canClose.get();
    }

    @FXML
    private void showRecentFile() {
        recentFilesMenu.getItems().clear();

        Deque<File> recentFiles = FileHandler.INSTANCE.getPreviousFiles();

        if (recentFiles.isEmpty()) {
            MenuItem none = new MenuItem("No recent files");
            none.setDisable(true);
            recentFilesMenu.getItems().add(none);
            return;
        }

        for (File file : recentFiles) {
            MenuItem item = getMenuItem(file);
            recentFilesMenu.getItems().add(item);
        }
    }

    private MenuItem getMenuItem(File file) {
        MenuItem item = new MenuItem(file.getName());
        item.setOnAction(_ -> {
            if (file.exists()) {
                HistoryHandler.INSTANCE.perform(new OpenFileCommand(file, this));
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("File not found");
                alert.setHeaderText("Cannot open recent file");
                alert.setContentText("The file \"" + file.getName() + "\" does not exist.");
                alert.showAndWait();
            }
        });
        return item;
    }

    @FXML
    private void closeFile() {
        if (FileHandler.INSTANCE.getCurrentFile() == null) return;

        Alert closeAlert = new Alert(Alert.AlertType.CONFIRMATION);
        closeAlert.setTitle("Close File");
        closeAlert.setHeaderText("Are you sure you want to close the current file?");
        closeAlert.setContentText("Unsaved changes will be lost if you haven't saved.");

        ButtonType saveClose = new ButtonType("Save & Close");
        ButtonType closeNoSave = new ButtonType("Close Without Saving");
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        closeAlert.getButtonTypes().setAll(saveClose, closeNoSave, cancel);

        // Button styling
        closeAlert.getDialogPane().lookupButton(saveClose).setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        closeAlert.getDialogPane().lookupButton(closeNoSave).setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        closeAlert.getDialogPane().lookupButton(cancel).setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white;");

        closeAlert.showAndWait().ifPresent(result -> {
            if (result == saveClose) {
                FileHandler.INSTANCE.overwriteCurrentFile(); // save current file
                FileHandler.INSTANCE.closeCurrentFile();
                refreshCurrentView();
            } else if (result == closeNoSave) {
                FileHandler.INSTANCE.closeCurrentFile();
                refreshCurrentView();
            }
            // Cancel does nothing
        });
    }

    @FXML
    private void revertFile() {
        File currentFile = FileHandler.INSTANCE.getCurrentFile();
        if (currentFile == null) return;

        Alert revertAlert = new Alert(Alert.AlertType.CONFIRMATION);
        revertAlert.setTitle("Revert File");
        revertAlert.setHeaderText("Are you sure you want to revert the current file?");
        revertAlert.setContentText("All unsaved changes will be lost.");

        ButtonType revert = new ButtonType("Revert");
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        revertAlert.getButtonTypes().setAll(revert, cancel);

        // Button styling
        revertAlert.getDialogPane().lookupButton(revert).setStyle("-fx-background-color: #f44336; -fx-text-fill: white;"); // Red
        revertAlert.getDialogPane().lookupButton(cancel).setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white;"); // Grey

        revertAlert.showAndWait().ifPresent(result -> {
            if (result == revert) {
                FileHandler.INSTANCE.reloadCurrentFile();
                HistoryHandler.INSTANCE.perform(new OpenFileCommand(currentFile, this));
                refreshCurrentView();
            }
            // Cancel does nothing
        });
    }
}