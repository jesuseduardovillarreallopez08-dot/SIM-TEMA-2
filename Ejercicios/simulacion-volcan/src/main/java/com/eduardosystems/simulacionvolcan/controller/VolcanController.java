package com.eduardosystems.simulacionvolcan.controller;

import com.eduardosystems.simulacionvolcan.entity.RegistroVolcan;
import com.eduardosystems.simulacionvolcan.service.ErupcionService;
import com.eduardosystems.simulacionvolcan.service.SimulacionService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.*;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Random;

@Controller
public class VolcanController {

    @Autowired
    private ErupcionService erupcionService;

    @Autowired
    private SimulacionService simulacionService;

    @FXML private Canvas canvasMapa;
    @FXML private Pane contenedorCanvas;
    @FXML private Label lblEstado;
    @FXML private Label lblPersonas;
    @FXML private Label lblCasas;
    @FXML private Label lblAnimales;
    @FXML private Button btnAuto;

    @FXML private TextField txtPersonas;
    @FXML private TextField txtGanado;
    @FXML private TextField txtFauna;
    @FXML private TextField txtVelocidadLava;

    @FXML private TableView<RegistroVolcan> tablaHistorial;
    @FXML private TableColumn<RegistroVolcan, String> colId;
    @FXML private TableColumn<RegistroVolcan, String> colPaso;
    @FXML private TableColumn<RegistroVolcan, String> colPersonas;
    @FXML private TableColumn<RegistroVolcan, String> colCasas;
    @FXML private TableColumn<RegistroVolcan, String> colAnimales;
    @FXML private TableColumn<RegistroVolcan, String> colDestruccion;

    private char[][] mapa;
    private int pasoActual = 0;
    private Timeline timeline;
    private boolean enEjecucion = false;

    private int personasIniciales = 0;
    private int casasIniciales = 0;
    private int animalesIniciales = 0;
    private double velocidadKmH = 25.0;

    private final Random rand = new Random();

    @FXML
    public void initialize() {
        configurarTabla();
        configurarAutoSimulacion();
        configurarRedimensionamientoResponsivo();
        handleReiniciar(null);
    }

    private void configurarRedimensionamientoResponsivo() {
        if (contenedorCanvas != null && canvasMapa != null) {
            canvasMapa.widthProperty().bind(contenedorCanvas.widthProperty());
            canvasMapa.heightProperty().bind(contenedorCanvas.heightProperty());

            canvasMapa.widthProperty().addListener((obs, oldVal, newVal) -> renderizarMapa());
            canvasMapa.heightProperty().addListener((obs, oldVal, newVal) -> renderizarMapa());
        }
    }

    private void configurarTabla() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));
        colPaso.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getPasoSimulacion())));

        colPersonas.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("%d / %d (%.1f%%)", d.getValue().getPersonasVivas(), d.getValue().getPersonasTotales(), d.getValue().getPorcentajePersonasVivas())
        ));
        colCasas.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("%d / %d (%.1f%%)", d.getValue().getCasasIntactas(), d.getValue().getCasasTotales(), d.getValue().getPorcentajeCasasIntactas())
        ));
        colAnimales.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("%d / %d (%.1f%%)", d.getValue().getAnimalesVivos(), d.getValue().getAnimalesTotales(), d.getValue().getPorcentajeAnimalesVivos())
        ));
        colDestruccion.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("%.1f%%", d.getValue().getPorcentajeDestruccion())
        ));
    }

    private void configurarAutoSimulacion() {
        timeline = new Timeline(new KeyFrame(Duration.millis(200), e -> handleAvanzarPaso(null)));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    @FXML
    private void handleToggleAuto(ActionEvent event) {
        if (enEjecucion) {
            timeline.stop();
            btnAuto.setText("AUTO: PLAY");
            btnAuto.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        } else {
            timeline.play();
            btnAuto.setText("AUTO: PAUSE");
            btnAuto.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        }
        enEjecucion = !enEjecucion;
    }

    @FXML
    private void handleAvanzarPaso(ActionEvent event) {
        pasoActual++;
        erupcionService.propagarLava(mapa, velocidadKmH);
        simulacionService.guardarPaso(pasoActual, mapa, personasIniciales, casasIniciales, animalesIniciales);
        actualizarInterfaz();
    }

    @FXML
    private void handleAplicarPoblacion(ActionEvent event) {
        handleReiniciar(event);
    }

    @FXML
    private void handleReiniciar(ActionEvent event) {
        if (timeline != null) timeline.stop();
        enEjecucion = false;
        if (btnAuto != null) {
            btnAuto.setText("AUTO: PLAY");
            btnAuto.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        }
        pasoActual = 0;
        simulacionService.limpiarHistorial();

        int numPersonas = obtenerValorEntero(txtPersonas, 150);
        int numGanado = obtenerValorEntero(txtGanado, 80);
        int numFauna = obtenerValorEntero(txtFauna, 60);
        velocidadKmH = obtenerValorDouble(txtVelocidadLava, 25.0);

        // Carga desde el dataset CSV + entidades personalizadas extras
        mapa = erupcionService.crearMapaInicial(numPersonas, numGanado, numFauna);
        personasIniciales = erupcionService.contarPersonasTotalesASalvo(mapa);
        casasIniciales = erupcionService.contarEstructurasIntactas(mapa);
        animalesIniciales = erupcionService.contarAnimalesTotalesASalvo(mapa);

        actualizarInterfaz();
    }

    private int obtenerValorEntero(TextField field, int valorDefecto) {
        if (field == null || field.getText().trim().isEmpty()) return valorDefecto;
        try {
            return Math.max(0, Integer.parseInt(field.getText().trim()));
        } catch (NumberFormatException e) {
            return valorDefecto;
        }
    }

    private double obtenerValorDouble(TextField field, double valorDefecto) {
        if (field == null || field.getText().trim().isEmpty()) return valorDefecto;
        try {
            return Math.max(1.0, Double.parseDouble(field.getText().trim()));
        } catch (NumberFormatException e) {
            return valorDefecto;
        }
    }

    private void actualizarInterfaz() {
        renderizarMapa();

        int pVivas = erupcionService.contarPersonasTotalesASalvo(mapa);
        int cIntactas = erupcionService.contarEstructurasIntactas(mapa);
        int aVivos = erupcionService.contarAnimalesTotalesASalvo(mapa);

        double pPct = personasIniciales == 0 ? 100.0 : ((double) pVivas / personasIniciales) * 100.0;
        double cPct = casasIniciales == 0 ? 100.0 : ((double) cIntactas / casasIniciales) * 100.0;
        double aPct = animalesIniciales == 0 ? 100.0 : ((double) aVivos / animalesIniciales) * 100.0;

        if (lblPersonas != null) lblPersonas.setText(String.format("%d / %d (%.1f%%)", pVivas, personasIniciales, pPct));
        if (lblCasas != null) lblCasas.setText(String.format("%d / %d (%.1f%%)", cIntactas, casasIniciales, cPct));
        if (lblAnimales != null) lblAnimales.setText(String.format("%d / %d (%.1f%%)", aVivos, animalesIniciales, aPct));

        double dano = erupcionService.calcularPorcentajeDestruccion(mapa);
        if (lblEstado != null) {
            lblEstado.setText(String.format("Popocatépetl | Paso: %d | Velocidad Lava: %.1f km/h | Impacto: %.1f%%", pasoActual, velocidadKmH, dano));
        }

        if (tablaHistorial != null) {
            List<RegistroVolcan> lista = simulacionService.obtenerHistorial();
            tablaHistorial.setItems(FXCollections.observableArrayList(lista));
            if (!lista.isEmpty()) {
                tablaHistorial.scrollTo(lista.size() - 1);
            }
        }
    }

    private void renderizarMapa() {
        if (canvasMapa == null || mapa == null) return;

        GraphicsContext gc = canvasMapa.getGraphicsContext2D();
        double canvasW = canvasMapa.getWidth();
        double canvasH = canvasMapa.getHeight();

        if (canvasW <= 0 || canvasH <= 0) return;

        gc.clearRect(0, 0, canvasW, canvasH);

        double cellW = canvasW / (double) ErupcionService.COLUMNAS;
        double cellH = canvasH / (double) ErupcionService.FILAS;

        // Terreno
        gc.setFill(Color.web("#12240e"));
        gc.fillRect(0, 0, canvasW, canvasH);

        // Calles y Barrancas
        for (int r = 0; r < ErupcionService.FILAS; r++) {
            for (int c = 0; c < ErupcionService.COLUMNAS; c++) {
                double x = c * cellW;
                double y = r * cellH;
                if (mapa[r][c] == 'B') {
                    gc.setFill(Color.web("#0a0604"));
                    gc.fillRect(x, y, cellW, cellH);
                } else if (mapa[r][c] == '=') {
                    gc.setFill(Color.web("#263238"));
                    gc.fillRect(x, y, cellW, cellH);
                }
            }
        }

        // Volcán Popocatépetl
        double cX = canvasW / 2.0;
        double cY = 22.0 * cellH;

        LinearGradient cono = new LinearGradient(
                0, 0, 0, canvasH * 0.7, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#f5f5f5")),
                new Stop(0.10, Color.web("#4e342e")),
                new Stop(0.40, Color.web("#2c1d18")),
                new Stop(0.80, Color.web("#1b2e18")),
                new Stop(1.0, Color.web("#12240e"))
        );

        gc.setFill(cono);
        gc.beginPath();
        gc.moveTo(cX - (canvasW * 0.48), canvasH * 0.7);
        gc.quadraticCurveTo(cX - (canvasW * 0.15), canvasH * 0.35, cX - (10 * cellW), cY + 8);
        gc.lineTo(cX + (10 * cellW), cY + 8);
        gc.quadraticCurveTo(cX + (canvasW * 0.15), canvasH * 0.35, cX + (canvasW * 0.48), canvasH * 0.7);
        gc.closePath();
        gc.fill();

        // Cráter y Magma
        gc.setFill(Color.web("#080402"));
        gc.fillOval(cX - (11 * cellW), cY - 4, 22 * cellW, 5.5 * cellH);

        RadialGradient magma = new RadialGradient(
                0, 0, cX, cY + 5, 8 * cellW, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#ffffff")),
                new Stop(0.4, Color.web("#ffaa00")),
                new Stop(0.85, Color.web("#ff1700")),
                new Stop(1.0, Color.TRANSPARENT)
        );
        gc.setFill(magma);
        gc.fillOval(cX - (8 * cellW), cY + 1, 16 * cellW, 3.5 * cellH);

        // Flujos de Lava
        for (int r = 0; r < ErupcionService.FILAS; r++) {
            for (int c = 0; c < ErupcionService.COLUMNAS; c++) {
                if (mapa[r][c] == 'L') {
                    double x = c * cellW;
                    double y = r * cellH;

                    gc.setFill(Color.rgb(255, 60, 0, 0.45));
                    gc.fillRect(x - 1, y - 1, cellW + 2, cellH + 2);

                    gc.setFill(Color.web("#ff3d00"));
                    gc.fillRect(x, y, cellW, cellH);

                    gc.setFill(Color.web("#ffea00"));
                    gc.fillRect(x + 1, y + 1, Math.max(1, cellW - 2), Math.max(1, cellH - 2));
                }
            }
        }

        // Ceniza
        for (int i = 0; i < 22; i++) {
            double px = cX + (rand.nextDouble() * (18 * cellW) - (9 * cellW)) + (i * 2.2);
            double py = cY - (4 * cellH) - (i * 1.2 * cellH);
            double radio = (4.5 * cellW) + (i * 0.6 * cellW);

            RadialGradient nube = new RadialGradient(
                    0, 0, px, py, radio, false, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.rgb(70, 65, 60, 0.45)),
                    new Stop(0.7, Color.rgb(30, 25, 20, 0.2)),
                    new Stop(1.0, Color.TRANSPARENT)
            );
            gc.setFill(nube);
            gc.fillOval(px - radio, py - radio, radio * 2, radio * 2);
        }

        // Entidades y Vegetación
        for (int r = 0; r < ErupcionService.FILAS; r++) {
            for (int c = 0; c < ErupcionService.COLUMNAS; c++) {
                char cel = mapa[r][c];
                double x = c * cellW;
                double y = r * cellH;

                switch (cel) {
                    case 'C':
                        dibujarCasaVectorial(gc, x, y, Math.max(5, cellW * 1.5), Math.max(5, cellH * 1.5), Color.web("#ffffff"), Color.web("#d32f2f"));
                        break;
                    case 'T':
                        dibujarCasaVectorial(gc, x, y, Math.max(6, cellW * 1.8), Math.max(6, cellH * 1.8), Color.web("#fbc02d"), Color.web("#1976d2"));
                        break;
                    case 'E':
                        dibujarEdificio(gc, x, y, Math.max(8, cellW * 2.2), Math.max(9, cellH * 2.5), Color.web("#b0bec5"), Color.web("#263238"));
                        break;
                    case 'P':
                        gc.setFill(Color.web("#ffe0b2"));
                        gc.fillOval(x, y, Math.max(2.5, cellW * 0.7), Math.max(2.5, cellH * 0.7));
                        gc.setFill(Color.web("#1e88e5"));
                        gc.fillRect(x, y + (cellH * 0.7), Math.max(2.5, cellW * 0.7), Math.max(3.5, cellH));
                        break;
                    case 'A':
                        gc.setFill(Color.web("#6d4c41"));
                        gc.fillRect(x, y + 1, Math.max(4, cellW * 1.2), Math.max(3, cellH * 0.9));
                        break;
                    case 'F':
                        gc.setFill(Color.web("#8d6e63"));
                        gc.fillOval(x, y, Math.max(3.5, cellW), Math.max(3.5, cellH));
                        break;
                    case 'Y':
                        gc.setFill(Color.web("#33691e"));
                        gc.fillPolygon(
                                new double[]{x - 1, x + (cellW / 2.0), x + cellW + 1},
                                new double[]{y + (cellH * 1.5), y, y + (cellH * 1.5)},
                                3
                        );
                        gc.setFill(Color.web("#4e342e"));
                        gc.fillRect(x + (cellW * 0.35), y + (cellH * 1.5), Math.max(1, cellW * 0.3), cellH * 0.5);
                        break;
                    case 'u':
                        gc.setFill(Color.web("#558b2f"));
                        gc.fillOval(x, y + 1, Math.max(4, cellW * 1.2), Math.max(3.5, cellH));
                        break;
                    case 'D':
                        dibujarCasaVectorial(gc, x, y, Math.max(5, cellW * 1.5), Math.max(5, cellH * 1.5), Color.web("#616161"), Color.web("#e65100"));
                        break;
                    case 'X':
                        gc.setFill(Color.web("#111111"));
                        gc.fillRect(x, y, Math.max(5, cellW * 1.5), Math.max(5, cellH * 1.5));
                        break;
                }
            }
        }
    }

    private void dibujarCasaVectorial(GraphicsContext gc, double x, double y, double w, double h, Color pared, Color techo) {
        gc.setFill(pared);
        gc.fillRect(x, y + (h * 0.4), w, h * 0.6);

        gc.setFill(techo);
        gc.fillPolygon(
                new double[]{x - 1, x + (w / 2.0), x + w + 1},
                new double[]{y + (h * 0.4), y, y + (h * 0.4)},
                3
        );
    }

    private void dibujarEdificio(GraphicsContext gc, double x, double y, double w, double h, Color pared, Color techo) {
        gc.setFill(pared);
        gc.fillRect(x, y + 2, w, h - 2);

        gc.setFill(techo);
        gc.fillRect(x - 0.5, y, w + 1, 2);

        gc.setFill(Color.web("#fff59d"));
        gc.fillRect(x + 2, y + 4, Math.max(1.5, w * 0.2), Math.max(1.5, h * 0.2));
        gc.fillRect(x + w - 4, y + 4, Math.max(1.5, w * 0.2), Math.max(1.5, h * 0.2));
    }
}