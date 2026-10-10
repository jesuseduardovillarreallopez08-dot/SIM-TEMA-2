package com.eduardosystems.galacticshield.entity;

public class Asteroid {
    private double x;
    private double y;
    private double vx;
    private double vy;
    private double radius;

    public Asteroid(double x, double y, double vx, double vy, double radius) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.radius = radius;
    }

    // Mueve el asteroide según su velocidad
    public void updatePosition() {
        this.x += this.vx;
        this.y += this.vy;
    }

    // Clona el asteroide para que la simulación de Monte Carlo no afecte al asteroide real
    public Asteroid clone() {
        return new Asteroid(this.x, this.y, this.vx, this.vy, this.radius);
    }

    // Getters y Setters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public double getRadius() { return radius; }
}