package com.example.cst8412ohly0001assignment2;

import java.util.LinkedHashMap;

public class EditCellCommand extends Command {
    private final LinkedHashMap<String, String> row;
    private final String column;
    private final String oldValue;
    private final String newValue;

    public EditCellCommand(LinkedHashMap<String, String> row, String column, String newValue,
                           DatasetController controller) {
        this.row = row;
        this.column = column;
        this.oldValue = row.get(column);
        this.newValue = newValue;

        // Capture current UI context at creation
        captureUIContext(controller.pagination, controller.currentRowIndex);
    }

    @Override
    public void execute() {
        row.put(column, newValue);
    }

    @Override
    public void undo() {
        row.put(column, oldValue);
    }

    @Override
    public void restoreUIContext(DatasetController controller) {
        super.restoreUIContext(controller);
    }
}
