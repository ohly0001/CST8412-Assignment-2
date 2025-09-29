package com.example.cst8412ohly0001assignment2;

import javafx.scene.control.Pagination;

public abstract class Command {
    // UI context
    protected int paginationIndex = 0;
    protected int rowIndex = 0;

    // Capture context when the command is created
    public void captureUIContext(Pagination pagination, int rowIndex) {
        this.paginationIndex = pagination != null ? pagination.getCurrentPageIndex() : 0;
        this.rowIndex = rowIndex;
    }

    // Must be implemented by subclasses
    public abstract void execute();
    public abstract void undo();

    // Restore UI context after executing/undoing
    public void restoreUIContext(DatasetController controller) {
        if (controller.pagination != null) {
            controller.pagination.setCurrentPageIndex(paginationIndex);
        }
        controller.currentRowIndex = rowIndex;
        controller.refreshCurrentView();
    }
}


