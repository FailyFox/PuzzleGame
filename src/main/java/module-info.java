module com.puzzle.puzzle {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires java.desktop;
    requires javafx.swing;
    requires opencv;

    opens com.puzzle to javafx.fxml;
    exports com.puzzle;
}