module com.eduardosystems.galacticshield {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens com.eduardosystems.galacticshield to javafx.fxml;
    exports com.eduardosystems.galacticshield;
}