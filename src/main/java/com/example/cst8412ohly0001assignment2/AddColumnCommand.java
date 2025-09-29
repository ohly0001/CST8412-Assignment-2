package com.example.cst8412ohly0001assignment2;

import java.util.LinkedHashMap;
import java.util.List;

public class AddColumnCommand extends Command {
    private final String columnName;
    private final List<LinkedHashMap<String, String>> contents;
    private final List<String> schema;

    public AddColumnCommand(String columnName, List<LinkedHashMap<String, String>> contents, List<String> schema, DatasetController controller) {
        this.columnName = columnName;
        this.contents = contents;
        this.schema = schema;
        captureUIContext(controller.pagination, controller.currentRowIndex);
    }

    @Override
    public void execute() {
        schema.add(columnName);
        contents.forEach(row -> row.put(columnName, ""));
    }

    @Override
    public void undo() {
        schema.remove(columnName);
        contents.forEach(row -> row.remove(columnName));
    }
}
