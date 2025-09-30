module com.example.cst8412ohly0001assignment2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.csv;
    requires java.logging;
    requires org.yaml.snakeyaml;
    requires java.desktop;

    opens com.example.cst8412ohly0001assignment2 to javafx.fxml;
    exports com.example.cst8412ohly0001assignment2;
    exports com.example.cst8412ohly0001assignment2.Commands;
    opens com.example.cst8412ohly0001assignment2.Commands to javafx.fxml;
    exports com.example.cst8412ohly0001assignment2.Controllers;
    opens com.example.cst8412ohly0001assignment2.Controllers to javafx.fxml;
}