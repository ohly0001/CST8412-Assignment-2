package com.example.cst8412ohly0001assignment2;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class OpenFileCommand extends Command {

    private final File newFile;

    // Backup previous state
    private LinkedList<String> oldSchema;
    private LinkedList<LinkedHashMap<String, String>> oldContents;
    private File oldFile;
    private String oldView;

    public OpenFileCommand(File newFile, int paginationIndex, int rowIndex, DatasetController controller) {
        super(paginationIndex, rowIndex, controller);
        this.newFile = newFile;
    }

    @Override
    public void execute() {
        // Backup old state
        oldSchema = new LinkedList<>(FileHandler.INSTANCE.getSchema());
        oldContents = new LinkedList<>(FileHandler.INSTANCE.getContents());
        oldFile = FileHandler.INSTANCE.getCurrentFile();
        oldView = controller.currentView;
        rowIndex = controller.currentRowIndex;

        // Load new file
        FileHandler.INSTANCE.readFile(newFile);

        // Refresh UI
        controller.refreshCurrentView();
    }

    @Override
    public void undo() {
        // Restore old file state
        FileHandler.INSTANCE.setSchema(oldSchema);
        FileHandler.INSTANCE.setContents(oldContents);
        FileHandler.INSTANCE.setCurrentFile(oldFile);

        controller.currentView = oldView;
        controller.currentRowIndex = rowIndex;

        controller.refreshCurrentView();
    }
}