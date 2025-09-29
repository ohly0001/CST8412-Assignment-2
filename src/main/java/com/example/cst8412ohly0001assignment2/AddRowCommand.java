package com.example.cst8412ohly0001assignment2;

import java.util.LinkedHashMap;
import javafx.collections.ObservableList;

public class AddRowCommand extends Command {
    private final LinkedHashMap<String, String> row;
    private final ObservableList<LinkedHashMap<String, String>> contents;

    public AddRowCommand(LinkedHashMap<String, String> row, ObservableList<LinkedHashMap<String,String>> contents, int paginationIndex, int rowIndex, DatasetController controller) {
        super(paginationIndex, rowIndex, controller);
        this.row = row;
        this.contents = contents;
        captureUIContext(controller.pagination, controller.currentRowIndex);
    }

    @Override
    public void execute() {
        contents.add(row);
    }

    @Override
    public void undo() {
        contents.remove(row);
    }
}