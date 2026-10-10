package com.eduardosystems.galacticshield.entity;

public class ShieldSector {
    private int id;
    private double startAngle; // Ángulo inicial del sector
    private double endAngle;   // Ángulo final del sector
    private double energy;     // Energía/Salud del escudo (0 a 100)
    private double riskLevel;  // Nivel de riesgo calculado por Monte Carlo (0.0 a 1.0)

    public ShieldSector(int id, double startAngle, double endAngle) {
        this.id = id;
        this.startAngle = startAngle;
        this.endAngle = endAngle;
        this.energy = 100.0;
        this.riskLevel = 0.0;
    }

    public int getId() { return id; }
    public double getStartAngle() { return startAngle; }
    public double getEndAngle() { return endAngle; }
    public double getEnergy() { return energy; }
    public void setEnergy(double energy) { this.energy = energy; }
    public double getRiskLevel() { return riskLevel; }
    public void setRiskLevel(double riskLevel) { this.riskLevel = riskLevel; }
}