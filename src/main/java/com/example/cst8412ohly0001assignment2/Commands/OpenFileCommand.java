package com.example.cst8412ohly0001assignment2.Commands;

import com.example.cst8412ohly0001assignment2.Controllers.DatasetController;
import com.example.cst8412ohly0001assignment2.Controllers.FileHandler;
import javafx.scene.control.Label;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class OpenFileCommand extends Command {

    private final File newFile;

    // Backup previous state
    private LinkedList<String> oldSchema;
    private LinkedList<LinkedHashMap<String, String>> oldContents;
    private File oldFile;

    public OpenFileCommand(File newFile, DatasetController controller) {
        super(controller);
        this.newFile = newFile;
    }

    @Override
    public void execute() {
        // Backup old state
        oldSchema = new LinkedList<>(FileHandler.INSTANCE.getSchema());
        oldContents = new LinkedList<>(FileHandler.INSTANCE.getContents());
        oldFile = FileHandler.INSTANCE.getCurrentFile();
        view = controller.getCurrentView();
        rowIndex = controller.getCurrentRowIndex();
        FileHandler.INSTANCE.readFile(newFile);
        controller.getCurrentFilePathLabel().setText(newFile.getAbsolutePath());
    }

    @Override
    public void undo() {
        // Restore old file state
        FileHandler.INSTANCE.setSchema(oldSchema);
        FileHandler.INSTANCE.setContents(oldContents);
        FileHandler.INSTANCE.setCurrentFile(oldFile);
        controller.getCurrentFilePathLabel().setText(oldFile.getAbsolutePath());
    }
}