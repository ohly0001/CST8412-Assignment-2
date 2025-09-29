package com.example.cst8412ohly0001assignment2;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class RemoveColumnCommand implements Command {
    private final String columnName;
    private final List<String> oldValues = new ArrayList<>();

    public RemoveColumnCommand(String columnName) {
        this.columnName = columnName;
    }

    @Override
    public void execute() {
        // store old values for undo
        FileHandler.INSTANCE.getContents().forEach(row ->
            oldValues.add(row.get(columnName))
        );
        FileHandler.INSTANCE.removeColumn(columnName);
    }

    @Override
    public void undo() {
        FileHandler.INSTANCE.addColumn(columnName);
        List<LinkedHashMap<String,String>> contents = FileHandler.INSTANCE.getContents();
        for (int i = 0; i < contents.size(); i++) {
            contents.get(i).put(columnName, oldValues.get(i));
        }
    }
}
