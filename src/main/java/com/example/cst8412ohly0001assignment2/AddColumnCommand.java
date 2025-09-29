package com.example.cst8412ohly0001assignment2;

public record AddColumnCommand(String columnName) implements Command {

    @Override
    public void execute() {
        FileHandler.INSTANCE.addColumn(columnName);
    }

    @Override
    public void undo() {
        FileHandler.INSTANCE.removeColumn(columnName);
    }
}
