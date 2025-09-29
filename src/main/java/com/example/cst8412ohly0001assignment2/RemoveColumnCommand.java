package com.example.cst8412ohly0001assignment2;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.HashMap;

public class RemoveColumnCommand extends Command {
    private final String columnName;
    private final List<LinkedHashMap<String, String>> contents;
    private final List<String> schema;
    private final HashMap<LinkedHashMap<String, String>, String> backupValues = new HashMap<>();

    public RemoveColumnCommand(String columnName, List<LinkedHashMap<String, String>> contents, List<String> schema, DatasetController controller) {
        this.columnName = columnName;
        this.contents = contents;
        this.schema = schema;
        captureUIContext(controller.pagination, controller.currentRowIndex);
    }

    @Override
    public void execute() {
        schema.remove(columnName);
        contents.forEach(row -> backupValues.put(row, row.remove(columnName)));
    }

    @Override
    public void undo() {
        schema.add(columnName);
        contents.forEach(row -> row.put(columnName, backupValues.get(row)));
    }
}
