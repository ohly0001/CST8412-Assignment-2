package com.example.cst8412ohly0001assignment2.Commands;

import java.util.LinkedHashMap;

import com.example.cst8412ohly0001assignment2.Controllers.DatasetController;
import com.example.cst8412ohly0001assignment2.Controllers.FileHandler;
import javafx.collections.ObservableList;

public class RemoveRowCommand extends Command {
    private final LinkedHashMap<String, String> row;
    private final int oldRowIndex;

    public RemoveRowCommand(int oldRowIndex, LinkedHashMap<String, String> row, DatasetController controller) {
        super(controller);
        this.row = row;
        this.oldRowIndex = oldRowIndex;
    }

    @Override
    public void execute() {
        FileHandler.INSTANCE.getContents().remove(row);
    }

    @Override
    public void undo() {
        FileHandler.INSTANCE.getContents().add(oldRowIndex, row);
    }
}