module com.eduardosystems.simulaciondamas {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.beans;
    requires spring.core;

    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires spring.data.jpa;

    // Permitir acceso de reflexión completo a Spring, JavaFX y Hibernate
    opens com.eduardosystems.simulaciondamas;
    opens com.eduardosystems.simulaciondamas.controller;
    opens com.eduardosystems.simulaciondamas.entity;
    opens com.eduardosystems.simulaciondamas.service;
    opens com.eduardosystems.simulaciondamas.repository;

    exports com.eduardosystems.simulaciondamas;
}