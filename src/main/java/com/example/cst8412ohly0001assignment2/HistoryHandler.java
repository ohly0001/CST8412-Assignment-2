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
        if (undoStack.size() >= maxHistory) undoStack.removeFirst();
        undoStack.addLast(command);
        command.restoreUIContext(command.controller);
    }

    public void undo(DatasetController controller) {
        if (!undoStack.isEmpty()) {
            Command cmd = undoStack.removeLast();
            cmd.undo();
            redoStack.addLast(cmd);
            cmd.restoreUIContext(controller);
        }
    }

    public void redo(DatasetController controller) {
        if (!redoStack.isEmpty()) {
            Command cmd = redoStack.removeLast();
            cmd.execute();
            undoStack.addLast(cmd);
            cmd.restoreUIContext(controller);
        }
    }
}