package com.example.cst8412ohly0001assignment2;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.io.File;

public class CloseFileCommand extends Command {
    // Backup previous state
    private LinkedList<String> oldSchema;
    private LinkedList<LinkedHashMap<String,String>> oldContents;
    private File oldFile;
    private String oldView;

    public CloseFileCommand(int paginationIndex, int rowIndex, DatasetController controller) {
        super(paginationIndex, rowIndex, controller);
    }

    @Override
    public void execute() {
        oldSchema = new LinkedList<>(FileHandler.INSTANCE.getSchema());
        oldContents = new LinkedList<>(FileHandler.INSTANCE.getContents());
        oldFile = FileHandler.INSTANCE.getCurrentFile();
        oldView = controller.currentView;
        rowIndex = controller.currentRowIndex;
        paginationIndex = controller.pagination != null ? controller.pagination.getCurrentPageIndex() : 0;

        FileHandler.INSTANCE.closeCurrentFile();
        controller.refreshCurrentView();
    }

    @Override
    public void undo() {
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

