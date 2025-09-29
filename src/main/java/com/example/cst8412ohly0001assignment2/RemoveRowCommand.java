package com.example.cst8412ohly0001assignment2;

import java.util.LinkedHashMap;
import javafx.collections.ObservableList;

public class RemoveRowCommand extends Command {
    private final LinkedHashMap<String, String> row;
    private final ObservableList<LinkedHashMap<String, String>> contents;

    public RemoveRowCommand(LinkedHashMap<String, String> row, int index, ObservableList<LinkedHashMap<String,String>> contents, int paginationIndex, int rowIndex, DatasetController controller) {
        super(paginationIndex, rowIndex, controller);
        this.row = row;
        this.index = index;
        this.contents = contents;
    }

    @Override
    public void execute() {
        contents.remove(row);
    }

    @Override
    public void undo() {
        contents.add(rowIndex, row);
    }
}