package com.eduardosystems.simulacionvolcan.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "registros_volcan")
public class RegistroVolcan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int pasoSimulacion;
    private double porcentajeDestruccion;
    private String estadoSimulacion;

    // Métricas detalladas
    private int personasVivas;
    private int personasTotales;
    private int casasIntactas;
    private int casasTotales;
    private int animalesVivos;
    private int animalesTotales;

    public RegistroVolcan() {}

    public RegistroVolcan(int pasoSimulacion, double porcentajeDestruccion, String estadoSimulacion,
                          int personasVivas, int personasTotales,
                          int casasIntactas, int casasTotales,
                          int animalesVivos, int animalesTotales) {
        this.pasoSimulacion = pasoSimulacion;
        this.porcentajeDestruccion = porcentajeDestruccion;
        this.estadoSimulacion = estadoSimulacion;
        this.personasVivas = personasVivas;
        this.personasTotales = personasTotales;
        this.casasIntactas = casasIntactas;
        this.casasTotales = casasTotales;
        this.animalesVivos = animalesVivos;
        this.animalesTotales = animalesTotales;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getPasoSimulacion() { return pasoSimulacion; }
    public void setPasoSimulacion(int pasoSimulacion) { this.pasoSimulacion = pasoSimulacion; }

    public double getPorcentajeDestruccion() { return porcentajeDestruccion; }
    public void setPorcentajeDestruccion(double porcentajeDestruccion) { this.porcentajeDestruccion = porcentajeDestruccion; }

    public String getEstadoSimulacion() { return estadoSimulacion; }
    public void setEstadoSimulacion(String estadoSimulacion) { this.estadoSimulacion = estadoSimulacion; }

    public int getPersonasVivas() { return personasVivas; }
    public int getPersonasTotales() { return personasTotales; }
    public int getCasasIntactas() { return casasIntactas; }
    public int getCasasTotales() { return casasTotales; }
    public int getAnimalesVivos() { return animalesVivos; }
    public int getAnimalesTotales() { return animalesTotales; }

    public double getPorcentajePersonasVivas() {
        return personasTotales == 0 ? 100.0 : ((double) personasVivas / personasTotales) * 100.0;
    }

    public double getPorcentajeCasasIntactas() {
        return casasTotales == 0 ? 100.0 : ((double) casasIntactas / casasTotales) * 100.0;
    }

    public double getPorcentajeAnimalesVivos() {
        return animalesTotales == 0 ? 100.0 : ((double) animalesVivos / animalesTotales) * 100.0;
    }
}