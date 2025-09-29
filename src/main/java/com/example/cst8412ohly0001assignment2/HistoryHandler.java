package com.example.cst8412ohly0001assignment2;

import java.util.ArrayDeque;
import java.util.Deque;

public class HistoryHandler {

    public static final HistoryHandler INSTANCE = new HistoryHandler();

    private final int maxHistory = 100;
    private final Deque<Command> undoStack = new ArrayDeque<>(maxHistory);
    private final Deque<Command> redoStack = new ArrayDeque<>();

    private HistoryHandler() {}

    /** Perform a command, clear redo history, and keep max history size */
    public void perform(Command command) {
        command.execute();
        redoStack.clear();

        if (undoStack.size() >= maxHistory) {
            undoStack.removeFirst(); // remove oldest command
        }

        undoStack.addLast(command);
    }

    /** Undo the last command */
    public void undo() {
        if (!undoStack.isEmpty()) {
            Command command = undoStack.removeLast(); // pop
            command.undo();
            redoStack.addLast(command);
        }
    }

    /** Redo the last undone command */
    public void redo() {
        if (!redoStack.isEmpty()) {
            Command command = redoStack.removeLast();
            command.execute();
            undoStack.addLast(command);
        }
    }
}