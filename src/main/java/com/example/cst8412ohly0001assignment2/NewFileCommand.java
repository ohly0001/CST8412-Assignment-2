package com.example.cst8412ohly0001assignment2;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class NewFileCommand extends Command {

    private final File newFile;

    // Backup previous state
    private LinkedList<String> oldSchema;
    private LinkedList<LinkedHashMap<String, String>> oldContents;
    private File oldFile;
    private String oldView;

    public NewFileCommand(File newFile, int paginationIndex, int rowIndex, DatasetController controller) {
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
        paginationIndex = controller.pagination != null ? controller.pagination.getCurrentPageIndex() : 0;

        // Perform new file creation
        FileHandler.INSTANCE.readFile(newFile);
        FileHandler.INSTANCE.clearContents();
        controller.showSchemaEditor();
    }

    @Override
    public void undo() {
        // Restore previous state
        FileHandler.INSTANCE.setSchema(oldSchema);
        FileHandler.INSTANCE.setContents(oldContents);
        FileHandler.INSTANCE.setCurrentFile(oldFile);

        controller.currentView = oldView;
        controller.currentRowIndex = rowIndex;
        if (controller.pagination != null)
            controller.pagination.setCurrentPageIndex(paginationIndex);

        controller.refreshCurrentView();
    }
}
