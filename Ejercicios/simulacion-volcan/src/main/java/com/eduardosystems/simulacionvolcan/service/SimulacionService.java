package com.eduardosystems.simulacionvolcan.service;

import com.eduardosystems.simulacionvolcan.entity.RegistroVolcan;
import com.eduardosystems.simulacionvolcan.repository.VolcanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SimulacionService {

    @Autowired
    private VolcanRepository repository;

    @Autowired
    private ErupcionService erupcionService;

    public RegistroVolcan guardarPaso(int paso, char[][] mapa,
                                      int personasIniciales, int casasIniciales, int animalesIniciales) {

        int pVivas = erupcionService.contarPersonasTotalesASalvo(mapa);
        int cIntactas = erupcionService.contarEstructurasIntactas(mapa);
        int aVivos = erupcionService.contarAnimalesTotalesASalvo(mapa);

        double destruccion = erupcionService.calcularPorcentajeDestruccion(mapa);
        String estado = pVivas == 0 ? "EVACUACION_COMPLETA_O_PERDIDA" : "ERUPCION_ACTIVA";

        RegistroVolcan registro = new RegistroVolcan(
                paso, destruccion, estado,
                pVivas, personasIniciales,
                cIntactas, casasIniciales,
                aVivos, animalesIniciales
        );

        return repository.save(registro);
    }

    public List<RegistroVolcan> obtenerHistorial() {
        return repository.findAll();
    }

    public void limpiarHistorial() {
        repository.deleteAll();
    }
}