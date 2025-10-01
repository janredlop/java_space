package com.solarsim;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

import java.util.ArrayDeque;
import java.util.Deque;

public class Planet {
    private final Sphere sphere;
    private final Group trailGroup;
    private final double radius;
    private final Color color;

    private String name;
    private double mass;
    private double distanceKm;

    private final Vector3D position;
    private final Vector3D velocity;

    private static final double KM_TO_UNITS = 1e-6;
    private static final double TRAIL_SEGMENT_MIN_DISTANCE = 20000; // km
    private static final int MAX_TRAIL_SEGMENTS = 600;
    private final Deque<Node> trailSegments = new ArrayDeque<>();
    private Vector3D lastTrailPosition;

    public Planet(String name, double mass, double distanceKm, double radius, Color color) {
        this.name = name;
        this.mass = mass;
        this.distanceKm = distanceKm;
        this.radius = radius;
        this.color = color;

        this.position = new Vector3D(distanceKm, 0, 0);
        this.velocity = new Vector3D(0, 0, 0);

        this.sphere = new Sphere(radius);
        PhongMaterial material = new PhongMaterial(color);
        material.setSpecularColor(color.brighter());
        this.sphere.setMaterial(material);

        this.trailGroup = new Group();
        updateNodePosition();
        this.lastTrailPosition = position.copy();
    }

    public Sphere getSphere() {
        return sphere;
    }

    public Group getTrailGroup() {
        return trailGroup;
    }

    public Vector3D getPosition() {
        return position;
    }

    public Vector3D getVelocity() {
        return velocity;
    }

    public double getMass() {
        return mass;
    }

    public String getName() {
        return name;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setMass(double mass) {
        this.mass = mass;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public double getRadius() {
        return radius;
    }

    public Color getColor() {
        return color;
    }

    public void updateNodePosition() {
        sphere.setTranslateX(position.x * KM_TO_UNITS);
        sphere.setTranslateY(position.y * KM_TO_UNITS);
        sphere.setTranslateZ(position.z * KM_TO_UNITS);
    }

    public void resetVelocity(Vector3D newVelocity) {
        this.velocity.x = newVelocity.x;
        this.velocity.y = newVelocity.y;
        this.velocity.z = newVelocity.z;
    }

    public void resetPosition(Vector3D newPosition) {
        this.position.x = newPosition.x;
        this.position.y = newPosition.y;
        this.position.z = newPosition.z;
        updateNodePosition();
        clearTrail();
        this.lastTrailPosition = newPosition.copy();
    }

    public void appendTrailIfNeeded() {
        double distance = distanceBetween(position, lastTrailPosition);
        if (distance < TRAIL_SEGMENT_MIN_DISTANCE) {
            return;
        }
        Cylinder segment = buildTrailSegment(lastTrailPosition, position);
        trailSegments.addLast(segment);
        trailGroup.getChildren().add(segment);
        lastTrailPosition = position.copy();
        while (trailSegments.size() > MAX_TRAIL_SEGMENTS) {
            Node removed = trailSegments.removeFirst();
            trailGroup.getChildren().remove(removed);
        }
    }

    private Cylinder buildTrailSegment(Vector3D start, Vector3D end) {
        Vector3D diff = new Vector3D(end.x - start.x, end.y - start.y, end.z - start.z);
        double distanceKm = diff.length();
        double distanceUnits = distanceKm * KM_TO_UNITS;

        Cylinder cylinder = new Cylinder(radius * 0.2, distanceUnits);
        cylinder.setMaterial(new PhongMaterial(color.deriveColor(0, 1, 0.6, 0.8)));

        double midX = (start.x + end.x) / 2.0 * KM_TO_UNITS;
        double midY = (start.y + end.y) / 2.0 * KM_TO_UNITS;
        double midZ = (start.z + end.z) / 2.0 * KM_TO_UNITS;
        cylinder.setTranslateX(midX);
        cylinder.setTranslateY(midY);
        cylinder.setTranslateZ(midZ);

        // Rotate cylinder to align with vector diff
        Vector3D axis = new Vector3D(diff.x, diff.y, diff.z);
        if (axis.length() > 0) {
            axis.normalize();
            double pitch = Math.atan2(axis.y, Math.sqrt(axis.x * axis.x + axis.z * axis.z));
            double yaw = Math.atan2(axis.x, axis.z);

            Rotate rotateY = new Rotate(Math.toDegrees(yaw), Rotate.Y_AXIS);
            Rotate rotateX = new Rotate(-Math.toDegrees(pitch), Rotate.X_AXIS);
            cylinder.getTransforms().addAll(rotateY, rotateX);
        }

        return cylinder;
    }

    private double distanceBetween(Vector3D a, Vector3D b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        double dz = a.z - b.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public void clearTrail() {
        trailSegments.clear();
        trailGroup.getChildren().clear();
        lastTrailPosition = position.copy();
    }
}
