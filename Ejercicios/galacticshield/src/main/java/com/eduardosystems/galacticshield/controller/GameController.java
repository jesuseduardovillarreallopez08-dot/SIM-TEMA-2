package com.eduardosystems.galacticshield.controller;

import com.eduardosystems.galacticshield.entity.Asteroid;
import com.eduardosystems.galacticshield.entity.ShieldSector;
import com.eduardosystems.galacticshield.service.MonteCarloService;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameController {
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final VBox controlPanel;

    // Entidades y Lógica
    private final List<Asteroid> asteroids = new ArrayList<>();
    private final List<ShieldSector> sectors = new ArrayList<>();
    private final MonteCarloService monteCarloService = new MonteCarloService();
    private final Random random = new Random();

    // Controles UI
    private Slider spawnRateSlider;
    private Slider simSpeedSlider;
    private Label survivalLabel;
    private Label breachLabel;
    private Label asteroidCountLabel;

    // Configuración Base Espacial
    private final double centerX = 400;
    private final double centerY = 300;
    private final double shieldRadius = 130;
    private double particleAngleOffset = 0;

    public GameController(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.controlPanel = createUIPanel();

        initShieldSectors();
        setupCanvasInteractions();
    }

    private void initShieldSectors() {
        sectors.add(new ShieldSector(0, 0, 90));
        sectors.add(new ShieldSector(1, 90, 180));
        sectors.add(new ShieldSector(2, 180, 270));
        sectors.add(new ShieldSector(3, 270, 360));
    }

    // --- PANEL DE CONTROL ESTILO SCI-FI (DASHBOARD) ---
    private VBox createUIPanel() {
        VBox panel = new VBox(12);
        panel.setPrefWidth(350);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #050b14; -fx-border-color: #00f0ff; -fx-border-width: 1.5px;");

        Label title = new Label("GALACTIC SHIELD:\nMONTE CARLO FORCE");
        title.setFont(Font.font("Consolas", 16));
        title.setTextFill(Color.CYAN);

        // Sliders de Interacción
        Label densityTitle = createStyledLabel("ASTEROID DENSITY");
        spawnRateSlider = new Slider(0.01, 0.10, 0.03);
        styleSlider(spawnRateSlider);

        Label speedTitle = createStyledLabel("SIMULATION SPEED");
        simSpeedSlider = new Slider(0.5, 2.5, 1.0);
        styleSlider(simSpeedSlider);

        // Indicadores de Estadísticas
        VBox statsBox = new VBox(8);
        statsBox.setStyle("-fx-border-color: #005577; -fx-padding: 10; -fx-background-color: #02050a;");

        survivalLabel = createStyledLabel("SURVIVAL CHANCE: 100.0%");
        survivalLabel.setTextFill(Color.LIME);

        breachLabel = createStyledLabel("SHIELD BREACH: 0.0%");
        breachLabel.setTextFill(Color.RED);

        asteroidCountLabel = createStyledLabel("TRACKED OBJECTS: 0");

        statsBox.getChildren().addAll(survivalLabel, breachLabel, asteroidCountLabel);

        // Botón Interactivo de Disparo Defensivo EMP
        Button empButton = new Button("TRIGGER EMP BLAST");
        empButton.setMaxWidth(Double.MAX_VALUE);
        empButton.setStyle("-fx-background-color: #00f0ff; -fx-text-fill: #000000; -fx-font-weight: bold; -fx-cursor: hand;");
        empButton.setOnAction(e -> triggerEMP());

        panel.getChildren().addAll(title, densityTitle, spawnRateSlider, speedTitle, simSpeedSlider, statsBox, empButton);
        return panel;
    }

    private Label createStyledLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Consolas", 12));
        lbl.setTextFill(Color.WHITE);
        return lbl;
    }

    private void styleSlider(Slider slider) {
        slider.setShowTickMarks(true);
        slider.setStyle("-fx-control-inner-background: #002233;");
    }

    private void setupCanvasInteractions() {
        // Clic en el canvas destruye asteroides en el punto cliqueado
        canvas.setOnMouseClicked(e -> {
            double mouseX = e.getX();
            double mouseY = e.getY();
            asteroids.removeIf(a -> Math.hypot(a.getX() - mouseX, a.getY() - mouseY) < 35);
        });
    }

    private void triggerEMP() {
        asteroids.clear();
    }

    // --- BUCLE PRINCIPAL DE JUEGO ---
    public void startGameLoop() {
        new AnimationTimer() {
            private long lastMonteCarloRun = 0;

            @Override
            public void handle(long now) {
                double speedMultiplier = simSpeedSlider.getValue();
                updateGameLogic(speedMultiplier);

                if (now - lastMonteCarloRun > 300_000_000L) {
                    monteCarloService.calculateRisk(asteroids, sectors, 250, centerX, centerY, shieldRadius);
                    updateDashboardStats();
                    lastMonteCarloRun = now;
                }

                render();
            }
        }.start();
    }

    private void updateGameLogic(double speedMultiplier) {
        particleAngleOffset += 0.02;

        // Aparición de asteroides dinámica por slider
        if (random.nextDouble() < spawnRateSlider.getValue() && asteroids.size() < 25) {
            double spawnX = random.nextBoolean() ? 0 : canvas.getWidth();
            double spawnY = random.nextDouble() * canvas.getHeight();

            double angle = Math.atan2(centerY - spawnY, centerX - spawnX) + (random.nextDouble() - 0.5) * 0.2;
            double speed = (1.2 + random.nextDouble() * 2.0) * speedMultiplier;

            asteroids.add(new Asteroid(spawnX, spawnY, Math.cos(angle) * speed, Math.sin(angle) * speed, 8));
        }

        // Movimiento de objetos
        for (int i = asteroids.size() - 1; i >= 0; i--) {
            Asteroid a = asteroids.get(i);
            a.updatePosition();

            double dist = Math.hypot(a.getX() - centerX, a.getY() - centerY);
            if (dist <= shieldRadius || a.getX() < -30 || a.getX() > canvas.getWidth() + 30) {
                asteroids.remove(i);
            }
        }
    }

    private void updateDashboardStats() {
        double maxRisk = sectors.stream().mapToDouble(ShieldSector::getRiskLevel).max().orElse(0.0);
        double breachChance = Math.min(100.0, maxRisk * 100);
        double survivalChance = Math.max(0.0, 100.0 - breachChance);

        survivalLabel.setText(String.format("SURVIVAL CHANCE: %.1f%%", survivalChance));
        breachLabel.setText(String.format("SHIELD BREACH: %.1f%%", breachChance));
        asteroidCountLabel.setText("TRACKED OBJECTS: " + asteroids.size());
    }

    // --- RENDERIZADO VISUAL TIPO HOLOGRAMA Y RESPLANDOR ---
    private void render() {
        // Fondo del espacio profundo
        gc.setFill(Color.web("#03060d"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Estrellas lejanas
        gc.setFill(Color.WHITE);
        for (int i = 0; i < 60; i++) {
            double starX = (i * 43) % canvas.getWidth();
            double starY = (i * 89) % canvas.getHeight();
            gc.fillOval(starX, starY, (i % 3 == 0) ? 2 : 1, (i % 3 == 0) ? 2 : 1);
        }

        // Rayos de simulación Monte Carlo (Vectores futuristas)
        gc.setLineWidth(1);
        for (Asteroid a : asteroids) {
            gc.setStroke(Color.web("#ff3366", 0.4));
            gc.setLineDashes(3);
            gc.strokeLine(a.getX(), a.getY(), a.getX() + a.getVx() * 35, a.getY() + a.getVy() * 35);
        }
        gc.setLineDashes(null);

        // Renderizado del Escudo Cúpula Holográfica
        gc.setEffect(new Glow(0.8));

        for (ShieldSector sector : sectors) {
            Color sectorColor = Color.web("#00f0ff").interpolate(Color.web("#ff0055"), sector.getRiskLevel());
            gc.setStroke(sectorColor);
            gc.setLineWidth(6);
            gc.strokeArc(centerX - shieldRadius, centerY - shieldRadius,
                    shieldRadius * 2, shieldRadius * 2,
                    sector.getStartAngle(), 90, ArcType.OPEN);
        }

        // Malla Holográfica de la Base Central
        gc.setStroke(Color.web("#00f0ff", 0.5));
        gc.setLineWidth(1.5);
        for (int i = 0; i < 3; i++) {
            double r = 30 + i * 25;
            gc.strokeOval(centerX - r, centerY - r / 2, r * 2, r);
        }

        // Núcleo Emisor de Energía
        gc.setFill(Color.web("#00f0ff"));
        gc.fillOval(centerX - 12, centerY - 12, 24, 24);

        // Asteroides de Fuego / Plasma
        for (Asteroid a : asteroids) {
            gc.setStroke(Color.web("#ff6600", 0.6));
            gc.setLineWidth(4);
            gc.strokeLine(a.getX(), a.getY(), a.getX() - a.getVx() * 6, a.getY() - a.getVy() * 6);

            gc.setFill(Color.web("#ffcc00"));
            gc.fillOval(a.getX() - a.getRadius(), a.getY() - a.getRadius(), a.getRadius() * 2, a.getRadius() * 2);
        }

        gc.setEffect(null);
    }

    public VBox getControlPanel() {
        return controlPanel;
    }
}