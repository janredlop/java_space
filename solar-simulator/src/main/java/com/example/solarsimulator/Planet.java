package com.example.solarsimulator;

import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

public class Planet {
    public static final double SCALE = 1e-6;

    private final Sphere node;
    private String name;
    private double mass;
    private double distanceFromSunKm;
    private Point3D position;
    private Point3D velocity;
    private double orbitalVelocity;

    public Planet(String name, double mass, double distanceFromSunKm, double initialSpeed) {
        this.name = name;
        this.mass = mass;
        this.distanceFromSunKm = distanceFromSunKm;
        this.orbitalVelocity = initialSpeed;
        this.position = new Point3D(distanceFromSunKm * SCALE, 0, 0);
        this.velocity = new Point3D(0, initialSpeed * SCALE, 0);
        this.node = new Sphere(8);
        PhongMaterial material = new PhongMaterial(Color.hsb(Math.random() * 360, 0.7, 0.9));
        node.setMaterial(material);
        updateNodePosition();
    }

    public void integrate(Point3D acceleration, double deltaSeconds) {
        velocity = velocity.add(acceleration.multiply(deltaSeconds));
        position = position.add(velocity.multiply(deltaSeconds));
        updateNodePosition();
    }

    public void resetOrbitForDistance(double centralMass, double gravitationalConstant) {
        double distanceMeters = distanceFromSunKm * 1000;
        orbitalVelocity = Math.sqrt(gravitationalConstant * centralMass / distanceMeters);
        position = new Point3D(distanceFromSunKm * SCALE, 0, 0);
        velocity = new Point3D(0, orbitalVelocity * SCALE, 0);
        updateNodePosition();
    }

    private void updateNodePosition() {
        node.setTranslateX(position.getX());
        node.setTranslateY(position.getY());
        node.setTranslateZ(position.getZ());
    }

    public Sphere getNode() {
        return node;
    }

    public Point3D getPosition() {
        return position;
    }

    public double getDistanceFromSunKm() {
        return distanceFromSunKm;
    }

    public double getCurrentDistanceKm() {
        return position.magnitude() / SCALE;
    }

    public double getCurrentDistanceMeters() {
        return getCurrentDistanceKm() * 1000;
    }

    public double getMass() {
        return mass;
    }

    public void setMass(double mass) {
        this.mass = mass;
    }

    public void setDistanceFromSunKm(double distanceFromSunKm) {
        this.distanceFromSunKm = distanceFromSunKm;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
