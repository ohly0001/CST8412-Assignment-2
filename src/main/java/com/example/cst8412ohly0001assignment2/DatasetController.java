package com.example.cst8412ohly0001assignment2;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Optional;

public class DatasetController {
    @FXML
    public VBox rootPane;

    @FXML Menu recentFilesMenu;

    @FXML
    private void newFile() {
        //TODO create empty file
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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");

        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
            new FileChooser.ExtensionFilter("JSON Files", "*.json"),
            new FileChooser.ExtensionFilter("XML Files", "*.xml"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Window owner = rootPane.getScene().getWindow();

        File file = fileChooser.showOpenDialog(owner);
        if (file != null) {
            FileHandler.INSTANCE.readFile(file);
        }
    }

    @FXML
    private void showRecentFile()
    {
        recentFilesMenu.setOnShowing(event -> {
            recentFilesMenu.getItems().clear();

            FileHandler.INSTANCE.getPreviousFiles().forEach(file -> {
                MenuItem item = new MenuItem(file.getName());
                item.setOnAction(e -> FileHandler.INSTANCE.readFile(file));
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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File As");

        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                new FileChooser.ExtensionFilter("XML Files", "*.xml"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Window owner = rootPane.getScene().getWindow();

        File file = fileChooser.showOpenDialog(owner);
        if (file != null) {
            FileHandler.INSTANCE.writeFile(file);
        }
    }
}