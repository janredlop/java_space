package com.example.solarsimulator;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

import java.util.LinkedList;
import java.util.Queue;

public class PlanetTrail {
    private final Group group = new Group();
    private final Color color;
    private final Queue<Sphere> spheres = new LinkedList<>();
    private final int maxPoints = 250;

    public PlanetTrail(Color color) {
        this.color = color;
    }

    public void addPoint(Point3D point) {
        Sphere sphere = new Sphere(0.8);
        sphere.setTranslateX(point.getX());
        sphere.setTranslateY(point.getY());
        sphere.setTranslateZ(point.getZ());
        sphere.setMaterial(new PhongMaterial(color.deriveColor(0, 1, 1, 0.7)));
        group.getChildren().add(sphere);
        spheres.add(sphere);
        if (spheres.size() > maxPoints) {
            Sphere old = spheres.poll();
            group.getChildren().remove(old);
        }
    }

    public void clear() {
        group.getChildren().clear();
        spheres.clear();
    }

    public Group getGroup() {
        return group;
    }
}
