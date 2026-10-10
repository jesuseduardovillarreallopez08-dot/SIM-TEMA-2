module com.eduardosystems.simulacionvolcan {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    requires spring.data.jpa;

    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    // Abrir todos los paquetes a Spring y JavaFX para la inyección de dependencias
    opens com.eduardosystems.simulacionvolcan to javafx.fxml, spring.core, spring.beans, spring.context;
    opens com.eduardosystems.simulacionvolcan.controller to javafx.fxml, spring.core, spring.beans, spring.context;
    opens com.eduardosystems.simulacionvolcan.entity to org.hibernate.orm.core, spring.core, spring.beans;
    opens com.eduardosystems.simulacionvolcan.service to spring.core, spring.beans, spring.context;
    opens com.eduardosystems.simulacionvolcan.repository to spring.core, spring.beans, spring.context;

    // Exportar paquetes
    exports com.eduardosystems.simulacionvolcan;
    exports com.eduardosystems.simulacionvolcan.controller;
    exports com.eduardosystems.simulacionvolcan.entity;
    exports com.eduardosystems.simulacionvolcan.service;
    exports com.eduardosystems.simulacionvolcan.repository;
}