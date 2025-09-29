package com.example.cst8412ohly0001assignment2;

import java.util.LinkedHashMap;
import java.util.LinkedList;

public class RemoveRowCommand implements Command {

    public LinkedHashMap<String, String> removedRow;
    private int rowIndex;

    public RemoveRowCommand(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    @Override
    public void execute() {
        LinkedList<LinkedHashMap<String, String>> contents = FileHandler.INSTANCE.getContents();
        if (rowIndex >= 0 && rowIndex < contents.size()) {
            removedRow = new LinkedHashMap<>(contents.get(rowIndex)); // copy for undo
            contents.remove(rowIndex);
        }
    }

    @Override
    public void undo() {
        if (removedRow != null) {
            LinkedList<LinkedHashMap<String, String>> contents = FileHandler.INSTANCE.getContents();
            if (rowIndex > contents.size()) {
                contents.addLast(removedRow);
            } else {
                contents.add(rowIndex, removedRow);
            }
        }
    }
}

