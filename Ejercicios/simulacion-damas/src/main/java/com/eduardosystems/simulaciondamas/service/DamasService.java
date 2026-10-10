package com.eduardosystems.simulaciondamas.service;

import com.eduardosystems.simulaciondamas.entity.Partida;
import com.eduardosystems.simulaciondamas.repository.PartidaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DamasService {

    @Autowired
    private PartidaRepository repository;

    @Autowired
    private IaService iaService;

    private static final String TABLERO_INICIAL =
            ".b.b.b.b" +
                    "b.b.b.b." +
                    ".b.b.b.b" +
                    "........" +
                    "........" +
                    "r.r.r.r." +
                    ".r.r.r.r" +
                    "r.r.r.r.";

    public Partida crearNuevaPartida() {
        Partida p = new Partida(TABLERO_INICIAL, "JUGADOR", "EN_PROGRESO");
        return repository.save(p);
    }

    public Partida hacerMovimientoJugador(Long id, int r1, int c1, int r2, int c2) {
        Partida partida = repository.findById(id).orElseThrow();
        if (!"EN_PROGRESO".equals(partida.getEstadoPartida())) return partida;

        char[][] grid = iaService.deserializar(partida.getEstadoTablero());

        boolean esCaptura = Math.abs(r1 - r2) == 2;
        int[] mov = new int[]{r1, c1, r2, c2, esCaptura ? 1 : 0};
        iaService.aplicarMovimiento(grid, mov);

        String nuevoTablero = iaService.serializar(grid);
        String tableroIA = iaService.calcularMejorMovimiento(nuevoTablero);

        partida.setEstadoTablero(tableroIA);
        partida.setTurnoActual("JUGADOR");
        return repository.save(partida);
    }

    public List<Partida> obtenerHistorial() {
        return repository.findAll();
    }
}