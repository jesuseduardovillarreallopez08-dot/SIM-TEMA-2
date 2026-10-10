package com.eduardosystems.simulaciondamas.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "partidas")
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String estadoTablero;
    private String turnoActual;
    private String estadoPartida;
    private LocalDateTime fechaCreacion;

    public Partida() {}

    public Partida(String estadoTablero, String turnoActual, String estadoPartida) {
        this.estadoTablero = estadoTablero;
        this.turnoActual = turnoActual;
        this.estadoPartida = estadoPartida;
        this.fechaCreacion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getEstadoTablero() { return estadoTablero; }
    public void setEstadoTablero(String estadoTablero) { this.estadoTablero = estadoTablero; }
    public String getTurnoActual() { return turnoActual; }
    public void setTurnoActual(String turnoActual) { this.turnoActual = turnoActual; }
    public String getEstadoPartida() { return estadoPartida; }
    public void setEstadoPartida(String estadoPartida) { this.estadoPartida = estadoPartida; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}