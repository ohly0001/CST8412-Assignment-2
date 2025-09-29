package com.example.cst8412ohly0001assignment2.Commands;

import com.example.cst8412ohly0001assignment2.Controllers.DatasetController;
import com.example.cst8412ohly0001assignment2.Controllers.FileHandler;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.io.File;

public class CloseFileCommand extends Command {
    // Backup previous state
    private LinkedList<String> oldSchema;
    private LinkedList<LinkedHashMap<String,String>> oldContents;
    private File oldFile;

    public CloseFileCommand(DatasetController controller) {
        super(controller);
    }

    @Override
    public void execute() {
        oldSchema = new LinkedList<>(FileHandler.INSTANCE.getSchema());
        oldContents = new LinkedList<>(FileHandler.INSTANCE.getContents());
        oldFile = FileHandler.INSTANCE.getCurrentFile();
        view = controller.getCurrentView();
        rowIndex = controller.getCurrentRowIndex();
        pageIndex = controller.getCurrentPageIndex();

        FileHandler.INSTANCE.closeCurrentFile();
    }

    @Override
    public void undo() {
        FileHandler.INSTANCE.setSchema(oldSchema);
        FileHandler.INSTANCE.setContents(oldContents);
        FileHandler.INSTANCE.setCurrentFile(oldFile);
    }
}

