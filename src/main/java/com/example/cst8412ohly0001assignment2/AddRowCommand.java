package com.example.cst8412ohly0001assignment2;

import java.util.LinkedHashMap;
import java.util.LinkedList;

public class AddRowCommand implements Command {

    public final LinkedHashMap<String, String> row; // row being added
    private int addedIndex = -1; // track where it was added

    public AddRowCommand(LinkedHashMap<String, String> row) {
        this.row = new LinkedHashMap<>(row); // make a copy
    }

    @Override
    public void execute() {
        LinkedList<LinkedHashMap<String, String>> contents = FileHandler.INSTANCE.getContents();
        contents.addLast(row);
        addedIndex = contents.size() - 1;
    }

    @Override
    public void undo() {
        if (addedIndex >= 0) {
            LinkedList<LinkedHashMap<String, String>> contents = FileHandler.INSTANCE.getContents();
            contents.remove(addedIndex);
        }
    }
}

