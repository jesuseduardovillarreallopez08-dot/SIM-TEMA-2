package com.eduardosystems.simulacionvolcan.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "dataset_poblacion")
public class DatasetPoblacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer fila;

    @Column(nullable = false)
    private Integer columna;

    @Column(nullable = false, length = 1)
    private Character tipo;

    public DatasetPoblacion() {}

    public DatasetPoblacion(Integer fila, Integer columna, Character tipo) {
        this.fila = fila;
        this.columna = columna;
        this.tipo = tipo;
    }

    public Long getId() { return id; }
    public Integer getFila() { return fila; }
    public void setFila(Integer fila) { this.fila = fila; }

    public Integer getColumna() { return columna; }
    public void setColumna(Integer columna) { this.columna = columna; }

    public Character getTipo() { return tipo; }
    public void setTipo(Character tipo) { this.tipo = tipo; }
}