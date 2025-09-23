package com.example.cst8412ohly0001assignment2;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

public class DatasetController {
    @FXML
    public VBox rootPane;

    @FXML Menu recentFilesMenu;

    private File selectFile(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);

        // Determine initial directory
        File lastFile = FileHandler.INSTANCE.getCurrentFile();
        String initialExtension = lastFile != null ? FileHandler.INSTANCE.getFileExtension(lastFile) : "";
        File initialDir = (lastFile != null && lastFile.getParentFile() != null)
                ? lastFile.getParentFile()
                : new File(System.getProperty("user.home"));
        if (initialDir.exists()) {
            fileChooser.setInitialDirectory(initialDir);
        }

        // Create filters
        FileChooser.ExtensionFilter allSafe = new FileChooser.ExtensionFilter("All Supported", "*.csv", "*.json", "*.yaml", "*.xml");
        FileChooser.ExtensionFilter csv = new FileChooser.ExtensionFilter("CSV Files", "*.csv");
        FileChooser.ExtensionFilter json = new FileChooser.ExtensionFilter("JSON Files", "*.json");
        FileChooser.ExtensionFilter yaml = new FileChooser.ExtensionFilter("YAML Files", "*.yaml");
        FileChooser.ExtensionFilter xml = new FileChooser.ExtensionFilter("XML Files", "*.xml");
        FileChooser.ExtensionFilter all = new FileChooser.ExtensionFilter("All Files", "*.*");

        // Add filters
        fileChooser.getExtensionFilters().addAll(allSafe, csv, json, yaml, xml, all);

        // Select default filter based on last file
        fileChooser.setSelectedExtensionFilter(switch (initialExtension) {
            case "csv" -> csv;
            case "json" -> json;
            case "yaml" -> yaml;
            case "xml" -> xml;
            default -> allSafe;
        });

        Window owner = rootPane.getScene().getWindow();
        return fileChooser.showOpenDialog(owner);
    }


    @FXML
    private void newFile()
    {
        File file = selectFile("New File");
        if (file != null) {
            FileHandler.INSTANCE.readFile(file);
        }
    }

    //TODO add record
    //same as view 1 record, but has a save button instead of arrows

    //TODO view 1 record at a time
    //display columns as labelled rows
    //change column values by clicking
    //navigate row by row with arrows or manual row # selection
    //has a delete button

    //TODO view all records as grid
    //tabular
    //view only for performance and summary safety
    //paginated every 100 rows for performance in GUI

    //TODO update schema of all records
    //add columns
    //remove columns

    @FXML
    private void openFile()
    {
        File file = selectFile("Open File");
        if (file != null) {
            FileHandler.INSTANCE.readFile(file);
        }
    }

    @FXML
    private void showRecentFile()
    {
        recentFilesMenu.setOnShowing(_ -> {
            recentFilesMenu.getItems().clear();

            FileHandler.INSTANCE.getPreviousFiles().forEach(file -> {
                MenuItem item = new MenuItem(file.getName());
                item.setOnAction(_ -> FileHandler.INSTANCE.readFile(file));
                recentFilesMenu.getItems().add(item);
            });
        });
    }

    @FXML
    private void quit() {
        Platform.exit();
    }

    @FXML
    private void saveFile() {
        FileHandler.INSTANCE.overwriteCurrentFile();
    }

    @FXML
    private void revertFile() {
        FileHandler.INSTANCE.reloadCurrentFile();
    }

    @FXML
    private void saveFileAs() {
        File file = selectFile("Save As");
        if (file != null) {
            FileHandler.INSTANCE.readFile(file);
        }
    }
}