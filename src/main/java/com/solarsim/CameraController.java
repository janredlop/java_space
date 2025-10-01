package com.solarsim;

import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Rotate;

public class CameraController {
    private final PerspectiveCamera camera;
    private final Rotate rotateX;
    private final Rotate rotateY;
    private double anchorX;
    private double anchorY;
    private double anchorAngleX;
    private double anchorAngleY;

    public CameraController(SubScene scene) {
        camera = new PerspectiveCamera(true);
        rotateX = new Rotate(-25, Rotate.X_AXIS);
        rotateY = new Rotate(-35, Rotate.Y_AXIS);
        camera.getTransforms().addAll(rotateY, rotateX);
        camera.setFarClip(5000);
        camera.setNearClip(0.1);
        camera.setTranslateZ(-600);
        scene.setCamera(camera);
        setupControls(scene);
    }

    private void setupControls(SubScene scene) {
        scene.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                anchorX = event.getSceneX();
                anchorY = event.getSceneY();
                anchorAngleX = rotateX.getAngle();
                anchorAngleY = rotateY.getAngle();
            }
        });

        scene.setOnMouseDragged(event -> {
            if (event.isSecondaryButtonDown()) {
                rotateY.setAngle(anchorAngleY + (anchorX - event.getSceneX()) * 0.3);
                rotateX.setAngle(anchorAngleX + (event.getSceneY() - anchorY) * 0.3);
            }
        });

        scene.addEventHandler(ScrollEvent.SCROLL, event -> {
            double zoom = camera.getTranslateZ() + event.getDeltaY() * 0.5;
            camera.setTranslateZ(Math.max(-2000, Math.min(-100, zoom)));
        });
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }
}
