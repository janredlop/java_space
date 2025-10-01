package com.example.solarsimulator;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import java.util.*;

public class SolarSystemSimulatorApp extends Application {

    private static final double G = 6.67430e-11;
    private static final double SUN_MASS = 1.9885e30;

    private final List<Planet> planets = new ArrayList<>();
    private final Map<Planet, PlanetTrail> trails = new HashMap<>();

    private PerspectiveCamera camera;
    private Group world;

    private double anchorX;
    private double anchorY;
    private double anchorAngleX;
    private double anchorAngleY;
    private final Rotate rotateX = new Rotate(-20, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(-20, Rotate.Y_AXIS);

    private AnimationTimer timer;
    private boolean paused = false;
    private double timeMultiplier = 1.0;
    private double elapsedSimulationSeconds = 0;

    private Label timeLabel;
    private Label speedLabel;

    private String gravitationalFormula = "G * centralMass / (distance * distance)";
    private final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");

    private VBox infoPanel;
    private Planet selectedPlanet;
    private Label currentDistanceLabel;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPrefSize(1200, 800);

        world = new Group();
        SubScene subScene = create3DScene();
        root.setCenter(subScene);

        HBox topBar = buildTopBar();
        root.setTop(topBar);

        infoPanel = buildInfoPanel();
        root.setRight(infoPanel);

        initializePlanets();

        Scene scene = new Scene(root);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE) {
                togglePause();
            }
        });

        primaryStage.setTitle("Simulador del Sistema Solar");
        primaryStage.setScene(scene);
        primaryStage.show();

        startSimulation();
    }

    private SubScene create3DScene() {
        Group sceneRoot = new Group();
        Sphere sun = new Sphere(25);
        PhongMaterial sunMaterial = new PhongMaterial(Color.GOLD);
        sun.setMaterial(sunMaterial);
        sceneRoot.getChildren().add(sun);

        world.getChildren().add(sceneRoot);

        SubScene subScene = new SubScene(world, 1200, 800, true, SceneAntialiasing.BALANCED);
        camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-600);
        camera.setNearClip(0.1);
        camera.setFarClip(10000);

        camera.getTransforms().addAll(rotateX, rotateY);

        subScene.setCamera(camera);
        subScene.setFill(Color.rgb(10, 10, 30));

        addMouseControls(subScene);

        return subScene;
    }

    private void addMouseControls(SubScene subScene) {
        subScene.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                anchorX = event.getSceneX();
                anchorY = event.getSceneY();
                anchorAngleX = rotateX.getAngle();
                anchorAngleY = rotateY.getAngle();
            }
        });

        subScene.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                rotateX.setAngle(anchorAngleX - (event.getSceneY() - anchorY) / 5);
                rotateY.setAngle(anchorAngleY + (event.getSceneX() - anchorX) / 5);
            }
        });

        subScene.setOnScroll(event -> {
            double delta = event.getDeltaY();
            camera.setTranslateZ(camera.getTranslateZ() + delta * 0.5);
        });
    }

    private HBox buildTopBar() {
        HBox hBox = new HBox(10);
        hBox.setPadding(new Insets(10));

        Button pauseButton = new Button("Pausar");
        pauseButton.setOnAction(e -> pauseSimulation());

        Button resumeButton = new Button("Continuar");
        resumeButton.setOnAction(e -> resumeSimulation());

        timeLabel = new Label("Tiempo: 0 s");
        speedLabel = new Label("Velocidad x1.0");

        Button slower = new Button("x0.5");
        slower.setOnAction(e -> setTimeMultiplier(Math.max(0.1, timeMultiplier / 2)));

        Button faster = new Button("x2");
        faster.setOnAction(e -> setTimeMultiplier(timeMultiplier * 2));

        Button configButton = new Button("Configuración");
        configButton.setOnAction(e -> showConfigurationDialog());

        hBox.getChildren().addAll(pauseButton, resumeButton, new Separator(), timeLabel, speedLabel, slower, faster, new Separator(), configButton);

        return hBox;
    }

    private VBox buildInfoPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(260);
        panel.setStyle("-fx-background-color: rgba(20,20,40,0.8); -fx-text-fill: white;");

        Label title = new Label("Planeta seleccionado");
        title.setTextFill(Color.WHITE);

        Label instructions = new Label("Haz clic en un planeta para editar su información.");
        instructions.setWrapText(true);
        instructions.setTextFill(Color.LIGHTGRAY);

        panel.getChildren().addAll(title, instructions);
        return panel;
    }

    private void showPlanetInfo(Planet planet) {
        infoPanel.getChildren().clear();

        Label title = new Label("Planeta seleccionado");
        title.setTextFill(Color.WHITE);

        TextField nameField = new TextField(planet.getName());
        TextField massField = new TextField(String.valueOf(planet.getMass()));
        TextField distanceField = new TextField(String.valueOf(planet.getDistanceFromSunKm()));

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.SALMON);

        currentDistanceLabel = new Label();
        currentDistanceLabel.setTextFill(Color.LIGHTBLUE);
        updateCurrentDistanceLabel(planet);

        Button apply = new Button("Aplicar cambios");
        apply.setOnAction(event -> {
            try {
                String newName = nameField.getText();
                double newMass = Double.parseDouble(massField.getText());
                double newDistance = Double.parseDouble(distanceField.getText());
                planet.setName(newName);
                planet.setMass(newMass);
                planet.setDistanceFromSunKm(newDistance);
                planet.resetOrbitForDistance(SUN_MASS, G);
                trails.get(planet).clear();
                errorLabel.setText("");
                updateCurrentDistanceLabel(planet);
            } catch (NumberFormatException ex) {
                errorLabel.setText("Valores numéricos inválidos");
            }
        });

        infoPanel.getChildren().addAll(title, new Label("Nombre:"), nameField,
                new Label("Masa (kg):"), massField,
                new Label("Distancia al Sol (km):"), distanceField,
                currentDistanceLabel,
                apply, errorLabel);

        selectedPlanet = planet;
    }

    private void updateCurrentDistanceLabel(Planet planet) {
        if (currentDistanceLabel != null) {
            currentDistanceLabel.setText(String.format(Locale.US, "Distancia actual: %.0f km", planet.getCurrentDistanceKm()));
        }
    }

    private void showConfigurationDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Configuración de gravedad");

        TextArea textArea = new TextArea(gravitationalFormula);
        textArea.setPrefColumnCount(40);
        textArea.setPrefRowCount(5);

        Label description = new Label("Formula en función de G, centralMass, distance y mass.");

        dialog.getDialogPane().setContent(new VBox(10, description, textArea));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.APPLY) {
                gravitationalFormula = textArea.getText();
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void initializePlanets() {
        createPlanet("Mercurio", 3.3011e23, 57_910_000, 47.87);
        createPlanet("Venus", 4.8675e24, 108_160_000, 35.02);
        createPlanet("Tierra", 5.97237e24, 149_600_000, 29.78);
        createPlanet("Marte", 6.4171e23, 227_990_000, 24.07);
        createPlanet("Júpiter", 1.8982e27, 778_330_000, 13.07);
        createPlanet("Saturno", 5.6834e26, 1_429_400_000, 9.69);
        createPlanet("Urano", 8.6810e25, 2_871_000_000L, 6.81);
        createPlanet("Neptuno", 1.02413e26, 4_495_000_000L, 5.43);
    }

    private void createPlanet(String name, double mass, double distanceKm, double orbitalVelocityKmPerSec) {
        Planet planet = new Planet(name, mass, distanceKm, orbitalVelocityKmPerSec * 1000);
        planets.add(planet);
        world.getChildren().add(planet.getNode());

        PlanetTrail trail = new PlanetTrail(Color.hsb(Math.random() * 360, 0.8, 1.0));
        trails.put(planet, trail);
        world.getChildren().add(trail.getGroup());

        planet.getNode().setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                showPlanetInfo(planet);
                event.consume();
            }
        });
    }

    private void startSimulation() {
        timer = new AnimationTimer() {
            private long lastUpdate = -1;

            @Override
            public void handle(long now) {
                if (paused) {
                    lastUpdate = now;
                    return;
                }

                if (lastUpdate < 0) {
                    lastUpdate = now;
                    return;
                }

                double deltaSeconds = ((now - lastUpdate) / 1_000_000_000.0) * timeMultiplier;
                lastUpdate = now;
                elapsedSimulationSeconds += deltaSeconds;
                updatePlanets(deltaSeconds);
                updateTimeLabel();
                if (selectedPlanet != null) {
                    updateCurrentDistanceLabel(selectedPlanet);
                }
            }
        };
        timer.start();
    }

    private void updatePlanets(double deltaSeconds) {
        for (Planet planet : planets) {
            Point3D acceleration = computeAcceleration(planet);
            planet.integrate(acceleration, deltaSeconds);
            trails.get(planet).addPoint(planet.getPosition());
        }
    }

    private Point3D computeAcceleration(Planet planet) {
        Point3D position = planet.getPosition();
        double distanceMeters = planet.getCurrentDistanceMeters();
        if (distanceMeters == 0) {
            return Point3D.ZERO;
        }

        double formulaValue;
        if (scriptEngine != null) {
            try {
                SimpleBindings bindings = new SimpleBindings();
                bindings.put("G", G);
                bindings.put("centralMass", SUN_MASS);
                bindings.put("distance", distanceMeters);
                bindings.put("mass", planet.getMass());
                formulaValue = ((Number) scriptEngine.eval(gravitationalFormula, bindings)).doubleValue();
            } catch (Exception e) {
                formulaValue = G * SUN_MASS / (distanceMeters * distanceMeters);
            }
        } else {
            formulaValue = G * SUN_MASS / (distanceMeters * distanceMeters);
        }

        Point3D direction = position.normalize().multiply(-1);
        double accelerationMagnitude = formulaValue;
        return direction.multiply(accelerationMagnitude);
    }

    private void updateTimeLabel() {
        timeLabel.setText(String.format(Locale.US, "Tiempo: %.2f s", elapsedSimulationSeconds));
        speedLabel.setText(String.format(Locale.US, "Velocidad x%.2f", timeMultiplier));
    }

    private void pauseSimulation() {
        paused = true;
    }

    private void resumeSimulation() {
        paused = false;
    }

    private void togglePause() {
        paused = !paused;
    }

    private void setTimeMultiplier(double multiplier) {
        this.timeMultiplier = multiplier;
    }

    private void stopSimulation() {
        if (timer != null) {
            timer.stop();
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        stopSimulation();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
