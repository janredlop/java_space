package com.solarsim;

import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.shape.Sphere;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.ArrayList;
import java.util.List;

public class SimulationEngine {
    private static final double DEFAULT_G = 6.67430e-20; // km^3 / (kg * s^2)
    private static final double SUN_MASS = 1.98847e30; // kg

    private final List<Planet> planets = new ArrayList<>();
    private final Group world = new Group();
    private final Sphere sun;

    private double elapsedTimeSeconds = 0;
    private double timeScale = 1.0;

    private String formulaExpression = "G * m1 * m2 / pow(r, 2)";
    private Expression compiledExpression;
    private double gravitationalConstant = DEFAULT_G;

    public SimulationEngine() {
        this.sun = new Sphere(20);
        sun.setMaterial(new javafx.scene.paint.PhongMaterial(Color.GOLD));
        world.getChildren().add(sun);

        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateZ(-50);
        world.getChildren().add(light);

        rebuildExpression();
        createDefaultPlanets();
    }

    public Group getWorld() {
        return world;
    }

    public List<Planet> getPlanets() {
        return planets;
    }

    public double getElapsedTimeSeconds() {
        return elapsedTimeSeconds;
    }

    public double getTimeScale() {
        return timeScale;
    }

    public void setTimeScale(double timeScale) {
        this.timeScale = Math.max(0.01, Math.min(timeScale, 1000));
    }

    public String getFormulaExpression() {
        return formulaExpression;
    }

    public void setFormulaExpression(String formulaExpression) {
        if (formulaExpression == null || formulaExpression.isBlank()) {
            throw new IllegalArgumentException("La fórmula no puede estar vacía");
        }
        this.formulaExpression = formulaExpression;
        rebuildExpression();
        resetOrbits();
    }

    public void update(double deltaSeconds) {
        if (deltaSeconds <= 0) {
            return;
        }
        double scaledDelta = deltaSeconds * timeScale;
        for (Planet planet : planets) {
            Vector3D pos = planet.getPosition();
            Vector3D vel = planet.getVelocity();

            Vector3D toSun = new Vector3D(-pos.x, -pos.y, -pos.z);
            double distance = toSun.length();
            if (distance < 1) {
                distance = 1;
            }
            double force = computeForce(SUN_MASS, planet.getMass(), distance);
            double accelerationMagnitude = Math.abs(force) / planet.getMass();
            Vector3D acceleration = toSun.copy().normalize().multiply(accelerationMagnitude);

            vel.add(acceleration.copy().multiply(scaledDelta));
            pos.add(vel.copy().multiply(scaledDelta));

            planet.updateNodePosition();
            planet.appendTrailIfNeeded();
        }
        elapsedTimeSeconds += scaledDelta;
    }

    public void updatePlanetParameters(Planet planet, String newName, double newMass, double newDistanceKm) {
        if (newMass <= 0 || newDistanceKm <= 0) {
            throw new IllegalArgumentException("Masa y distancia deben ser positivas");
        }
        planet.setName(newName.isBlank() ? planet.getName() : newName);
        planet.setMass(newMass);
        planet.setDistanceKm(newDistanceKm);
        reinitializeOrbit(planet);
    }

    private double computeForce(double m1, double m2, double r) {
        try {
            return compiledExpression
                    .setVariable("G", gravitationalConstant)
                    .setVariable("m1", m1)
                    .setVariable("m2", m2)
                    .setVariable("r", r)
                    .evaluate();
        } catch (Exception ex) {
            return DEFAULT_G * m1 * m2 / (r * r);
        }
    }

    private void rebuildExpression() {
        compiledExpression = new ExpressionBuilder(formulaExpression)
                .variables("G", "m1", "m2", "r")
                .build();
    }

    private void createDefaultPlanets() {
        planets.clear();
        addPlanet(new PlanetHolder(new Planet("Mercurio", 3.3011e23, 57_909_227, 2, Color.SILVER), 0));
        addPlanet(new PlanetHolder(new Planet("Venus", 4.8675e24, 108_209_475, 3, Color.BEIGE), 10));
        addPlanet(new PlanetHolder(new Planet("Tierra", 5.97237e24, 149_598_023, 3.5, Color.ROYALBLUE), 20));
        addPlanet(new PlanetHolder(new Planet("Marte", 6.4171e23, 227_939_200, 3, Color.ORANGERED), 35));
        addPlanet(new PlanetHolder(new Planet("Júpiter", 1.8982e27, 778_299_000, 8, Color.BURLYWOOD), 50));
        addPlanet(new PlanetHolder(new Planet("Saturno", 5.6834e26, 1_426_666_422, 7, Color.KHAKI), 65));
        addPlanet(new PlanetHolder(new Planet("Urano", 8.6810e25, 2_870_658_186d, 6, Color.LIGHTSKYBLUE), 80));
        addPlanet(new PlanetHolder(new Planet("Neptuno", 1.02413e26, 4_498_396_441d, 6, Color.DODGERBLUE), 95));

        resetOrbits();
    }

    private void addPlanet(PlanetHolder holder) {
        planets.add(holder.planet);
        world.getChildren().addAll(holder.planet.getTrailGroup(), holder.planet.getSphere());
        holder.planet.getSphere().setUserData(holder);
    }

    private void resetOrbits() {
        for (Planet planet : planets) {
            reinitializeOrbit(planet);
        }
    }

    private void reinitializeOrbit(Planet planet) {
        PlanetHolder holder = (PlanetHolder) planet.getSphere().getUserData();
        double baseAngle = holder != null ? holder.phaseAngle : 0;
        double distance = planet.getDistanceKm();
        double angleRad = Math.toRadians(baseAngle);

        double x = distance * Math.cos(angleRad);
        double z = distance * Math.sin(angleRad);
        double y = distance * Math.sin(Math.toRadians(holder != null ? holder.inclination : 0)) * 0.1;

        Vector3D newPosition = new Vector3D(x, y, z);
        double force = computeForce(SUN_MASS, planet.getMass(), distance);
        double acceleration = Math.abs(force) / planet.getMass();
        double velocityMagnitude = Math.sqrt(Math.max(acceleration * distance, 0));
        Vector3D newVelocity = new Vector3D(-Math.sin(angleRad) * velocityMagnitude,
                0,
                Math.cos(angleRad) * velocityMagnitude);

        planet.resetPosition(newPosition);
        planet.resetVelocity(newVelocity);
    }

    private static class PlanetHolder {
        final Planet planet;
        final double inclination;
        final double phaseAngle;

        PlanetHolder(Planet planet, double phaseAngle) {
            this(planet, phaseAngle, 5);
        }

        PlanetHolder(Planet planet, double phaseAngle, double inclination) {
            this.planet = planet;
            this.phaseAngle = phaseAngle;
            this.inclination = inclination;
        }
    }
}
