package com.example.cst8412ohly0001assignment2;

import java.util.LinkedHashMap;

public class EditCellCommand implements Command {
    public final LinkedHashMap<String, String> row;
    public final String column;
    public final String oldValue;
    public final String newValue;

    public EditCellCommand(LinkedHashMap<String, String> row, String column, String newValue) {
        this.row = row;
        this.column = column;
        this.oldValue = row.get(column);
        this.newValue = newValue;
    }

    @Override
    public void execute() {
        row.put(column, newValue);
    }

    @Override
    public void undo() {
        row.put(column, oldValue);
    }
}
