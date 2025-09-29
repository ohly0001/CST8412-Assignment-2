package com.example.cst8412ohly0001assignment2;

import java.util.LinkedHashMap;
import javafx.collections.ObservableList;

public class RemoveRowCommand extends Command {
    private final LinkedHashMap<String, String> row;
    private final ObservableList<LinkedHashMap<String, String>> contents;
    private final int index;

    public RemoveRowCommand(LinkedHashMap<String, String> row, int index, ObservableList<LinkedHashMap<String,String>> contents, DatasetController controller) {
        this.row = row;
        this.index = index;
        this.contents = contents;
        captureUIContext(controller.pagination, controller.currentRowIndex);
    }

    @Override
    public void execute() {
        contents.remove(row);
    }

    @Override
    public void undo() {
        contents.add(index, row);
    }
}