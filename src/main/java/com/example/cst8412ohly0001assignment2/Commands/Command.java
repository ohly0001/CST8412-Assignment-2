package com.example.cst8412ohly0001assignment2.Commands;

import com.example.cst8412ohly0001assignment2.Controllers.DatasetController;

public abstract class Command {
    // UI context
    protected int paginationIndex = 0;
    protected int rowIndex = 0;
    protected DatasetController controller;

    public Command(DatasetController controller) {
        this.paginationIndex = controller.pagination != null ? controller.pagination.getCurrentPageIndex() : 0;
        this.rowIndex = controller.currentRowIndex;
        this.controller = controller;
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


