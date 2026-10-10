package com.eduardosystems.simulaciondamas.controller;

import com.eduardosystems.simulaciondamas.entity.Partida;
import com.eduardosystems.simulaciondamas.service.DamasService;
import com.eduardosystems.simulaciondamas.service.IaService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class DamasController {

    @Autowired
    private DamasService damasService;

    @Autowired
    private IaService iaService;

    @FXML private Canvas canvasTablero;
    @FXML private Label lblEstado;
    @FXML private TableView<Partida> tablaHistorial;
    @FXML private TableColumn<Partida, String> colId;
    @FXML private TableColumn<Partida, String> colEstado;
    @FXML private TableColumn<Partida, String> colTurno;
    @FXML private TableColumn<Partida, String> colFecha;

    private Partida partidaActual;
    private int[] casillaSeleccionada = null;
    private static final double TILE_SIZE = 50.0;

    @FXML
    public void initialize() {
        configurarTabla();
    }

    public void iniciarJuego() {
        handleNuevaPartida();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colEstado.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEstadoPartida()));
        colTurno.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTurnoActual()));
        colFecha.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFechaCreacion() != null ?
                        data.getValue().getFechaCreacion().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : ""
        ));
    }

    @FXML
    private void handleNuevaPartida() {
        this.partidaActual = damasService.crearNuevaPartida();
        this.casillaSeleccionada = null;
        actualizarInterfaz();
    }

    @FXML
    private void handleCanvasClick(MouseEvent event) {
        if (partidaActual == null || !"EN_PROGRESO".equals(partidaActual.getEstadoPartida())) return;

        int c = (int) (event.getX() / TILE_SIZE);
        int r = (int) (event.getY() / TILE_SIZE);

        char[][] grid = iaService.deserializar(partidaActual.getEstadoTablero());

        if (casillaSeleccionada == null) {
            char ficha = grid[r][c];
            if (ficha == 'r' || ficha == 'R') {
                casillaSeleccionada = new int[]{r, c};
                renderizarTablero();
            }
        } else {
            int r1 = casillaSeleccionada[0];
            int c1 = casillaSeleccionada[1];

            this.partidaActual = damasService.hacerMovimientoJugador(partidaActual.getId(), r1, c1, r, c);
            this.casillaSeleccionada = null;
            actualizarInterfaz();
        }
    }

    private void actualizarInterfaz() {
        renderizarTablero();
        lblEstado.setText("Partida #" + partidaActual.getId() + " - Turno: " + partidaActual.getTurnoActual());
        tablaHistorial.setItems(FXCollections.observableArrayList(damasService.obtenerHistorial()));
    }

    private void renderizarTablero() {
        GraphicsContext gc = canvasTablero.getGraphicsContext2D();
        String estadoStr = partidaActual.getEstadoTablero();

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                gc.setFill((r + c) % 2 == 0 ? Color.web("#ffce9e") : Color.web("#d18b47"));
                gc.fillRect(c * TILE_SIZE, r * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                char ficha = estadoStr.charAt(r * 8 + c);
                if (ficha != '.') {
                    gc.setFill((ficha == 'r' || ficha == 'R') ? Color.web("#dc3545") : Color.web("#212529"));
                    gc.fillOval(c * TILE_SIZE + 7, r * TILE_SIZE + 7, 36, 36);
                    gc.setStroke(Color.WHITE);
                    gc.setLineWidth(2);
                    gc.strokeOval(c * TILE_SIZE + 7, r * TILE_SIZE + 7, 36, 36);

                    if (ficha == 'R' || ficha == 'B') {
                        gc.setFill(Color.web("#ffc107"));
                        gc.fillText("★", c * TILE_SIZE + 20, r * TILE_SIZE + 30);
                    }
                }
            }
        }

        if (casillaSeleccionada != null) {
            gc.setStroke(Color.web("#28a745"));
            gc.setLineWidth(3);
            gc.strokeRect(casillaSeleccionada[1] * TILE_SIZE, casillaSeleccionada[0] * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }
    }
}