package com.eduardosystems.galacticshield.service;

import com.eduardosystems.galacticshield.entity.Asteroid;
import com.eduardosystems.galacticshield.entity.ShieldSector;

import java.util.List;
import java.util.Random;

public class MonteCarloService {
    private final Random random = new Random();

    /**
     * Simula N futuras trayectorias de asteroides para calcular el riesgo en cada sector.
     * @param asteroids Lista de asteroides reales actuales.
     * @param sectors Lista de sectores del escudo.
     * @param simulations Number of Monte Carlo samples (ej. 1000).
     * @param shieldX Coordenada X del centro del escudo.
     * @param shieldY Coordenada Y del centro del escudo.
     * @param shieldRadius Radio del escudo.
     */
    public void calculateRisk(List<Asteroid> asteroids, List<ShieldSector> sectors, int simulations, double shieldX, double shieldY, double shieldRadius) {
        // Reiniciar contadores de riesgo
        int[] hitsPerSector = new int[sectors.size()];

        for (Asteroid asteroid : asteroids) {
            for (int i = 0; i < simulations; i++) {
                // Clonamos el asteroide e introducimos "incertidumbre" en su velocidad
                Asteroid simAsteroid = asteroid.clone();
                double noiseX = (random.nextDouble() - 0.5) * 0.5; // Pequeño ajuste aleatorio
                double noiseY = (random.nextDouble() - 0.5) * 0.5;

                // Avanzamos el asteroide sim de simulación hacia el futuro
                for (int step = 0; step < 100; step++) {
                    double currentX = simAsteroid.getX() + (simAsteroid.getVx() + noiseX) * step;
                    double currentY = simAsteroid.getY() + (simAsteroid.getVy() + noiseY) * step;

                    // Distancia al centro del escudo
                    double dist = Math.hypot(currentX - shieldX, currentY - shieldY);

                    if (dist <= shieldRadius) {
                        // Impacto detectado: calcular qué sector recibió el impacto según el ángulo
                        double angle = Math.toDegrees(Math.atan2(currentY - shieldY, currentX - shieldX));
                        if (angle < 0) angle += 360;

                        for (int s = 0; s < sectors.size(); s++) {
                            ShieldSector sector = sectors.get(s);
                            if (angle >= sector.getStartAngle() && angle < sector.getEndAngle()) {
                                hitsPerSector[s]++;
                                break;
                            }
                        }
                        break; // Terminamos la simulación de este rayo
                    }
                }
            }
        }

        // Actualizar el nivel de riesgo de cada sector de 0.0 a 1.0
        int totalSimulations = asteroids.size() * simulations;
        if (totalSimulations > 0) {
            for (int s = 0; s < sectors.size(); s++) {
                double risk = (double) hitsPerSector[s] / simulations;
                sectors.get(s).setRiskLevel(Math.min(risk, 1.0));
            }
        }
    }
}