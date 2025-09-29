package com.example.cst8412ohly0001assignment2.Commands;

import java.util.LinkedHashMap;

import com.example.cst8412ohly0001assignment2.Controllers.DatasetController;
import javafx.collections.ObservableList;

public class AddRowCommand extends Command {
    private final LinkedHashMap<String, String> row;
    private final ObservableList<LinkedHashMap<String, String>> contents;

    public AddRowCommand(LinkedHashMap<String, String> row, ObservableList<LinkedHashMap<String,String>> contents, DatasetController controller) {
        super(controller);
        this.row = row;
        this.contents = contents;
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