package com.example.cst8412ohly0001assignment2.Controllers;

import com.example.cst8412ohly0001assignment2.Commands.Command;

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
        command.getController().refreshCurrentView();
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            Command command = undoStack.removeLast();
            command.undo();
            redoStack.addLast(command);
            command.restoreUIContext();
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            Command command = redoStack.removeLast();
            command.execute();
            undoStack.addLast(command);
            command.restoreUIContext();
        }
    }
}