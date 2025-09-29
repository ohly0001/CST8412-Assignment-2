package com.example.cst8412ohly0001assignment2.Commands;

import com.example.cst8412ohly0001assignment2.Controllers.DatasetController;

public abstract class Command {
    // UI context
    protected int pageIndex = 0;
    protected int rowIndex = 0;
    protected String view = null;
    protected DatasetController controller;

    public Command(DatasetController controller) {
        this.pageIndex = controller.getCurrentPageIndex();
        this.rowIndex = controller.getCurrentRowIndex();
        this.view = controller.getCurrentView();
        this.controller = controller;
    }

    // Must be implemented by subclasses
    public abstract void execute();
    public abstract void undo();

    // Restore UI context after executing/undoing
    public void restoreUIContext() {
        controller.setCurrentView(view);
        controller.setCurrentPageIndex(pageIndex);
        controller.setCurrentRowIndex(rowIndex);
        controller.refreshCurrentView();
    }

    public DatasetController getController() {
        return controller;
    }
}


