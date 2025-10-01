package com.example.solarsystem;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SolarSystemSimulatorApp extends Application {

    private static final double SUN_MASS = 1.989e30; // kg
    private static final double DEFAULT_GRAVITATIONAL_CONSTANT = 6.67430e-11; // m^3 kg^-1 s^-2
    private static final double DISTANCE_SCALE = 1e9; // metres to render units
    private static final double TRAIL_POINT_DISTANCE = 0.02; // in rendered units

    private final Group world = new Group();
    private final Group trailLayer = new Group();
    private final List<Planet> planets = new ArrayList<>();

    private final ObjectProperty<Planet> selectedPlanet = new SimpleObjectProperty<>();

    private AnimationTimer timer;
    private boolean paused = false;
    private double timeScale = 1.0;
    private double simulationTimeSeconds = 0.0;

    private Label timeLabel;
    private Label speedLabel;
    private PlanetInfoPanel planetInfoPanel;

    private Rotate rotateX = new Rotate(-20, Rotate.X_AXIS);
    private Rotate rotateY = new Rotate(-30, Rotate.Y_AXIS);

    private double anchorX;
    private double anchorY;
    private double anchorAngleX;
    private double anchorAngleY;

    private PerspectiveCamera camera;

    private String gravitationalFormula = "G * m1 * m2 / (r * r)";
    private Expression gravitationalExpression;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0b1d33;");

        SubScene simulationScene = createSimulationSubScene();
        StackPane centerPane = new StackPane(simulationScene);
        simulationScene.widthProperty().bind(centerPane.widthProperty());
        simulationScene.heightProperty().bind(centerPane.heightProperty());
        root.setCenter(centerPane);

        HBox topBar = createTopBar(primaryStage);
        root.setTop(topBar);

        planetInfoPanel = new PlanetInfoPanel();
        root.setRight(planetInfoPanel.getView());

        Scene scene = new Scene(root, 1200, 800, true);
        primaryStage.setTitle("Simulador del Sistema Solar");
        primaryStage.setScene(scene);
        primaryStage.show();

        selectedPlanet.addListener((obs, oldPlanet, newPlanet) -> planetInfoPanel.displayPlanet(newPlanet));

        rebuildGravitationalExpression();
        initPlanets();
        initAnimationTimer();
    }

    private SubScene createSimulationSubScene() {
        Group root3d = new Group();
        world.getChildren().clear();
        trailLayer.getChildren().clear();

        Sphere sun = new Sphere(15);
        PhongMaterial sunMaterial = new PhongMaterial(Color.GOLD);
        sun.setMaterial(sunMaterial);

        PointLight pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateX(0);
        pointLight.setTranslateY(0);
        pointLight.setTranslateZ(0);

        AmbientLight ambientLight = new AmbientLight(Color.color(0.2, 0.2, 0.3));

        world.getChildren().addAll(trailLayer, sun, pointLight, ambientLight);

        root3d.getChildren().add(world);
        world.getTransforms().addAll(rotateX, rotateY);

        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(5000);
        camera.setTranslateZ(-200);

        SubScene subScene = new SubScene(root3d, 1000, 800, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.rgb(5, 10, 25));
        subScene.setCamera(camera);

        enableCameraControls(subScene);

        return subScene;
    }

    private void enableCameraControls(SubScene subScene) {
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
                rotateX.setAngle(anchorAngleX - (event.getSceneY() - anchorY) / 2);
                rotateY.setAngle(anchorAngleY + (event.getSceneX() - anchorX) / 2);
            }
        });

        subScene.addEventFilter(ScrollEvent.SCROLL, event -> {
            double delta = event.getDeltaY();
            double newZ = camera.getTranslateZ() - delta * 0.1;
            camera.setTranslateZ(Math.max(-1200, Math.min(newZ, -50)));
        });

        subScene.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                Node intersected = event.getPickResult() != null ? event.getPickResult().getIntersectedNode() : null;
                Object userData = intersected != null ? intersected.getUserData() : null;
                if (userData == null || !(userData instanceof Planet)) {
                    selectedPlanet.set(null);
                }
            }
        });
    }

    private HBox createTopBar(Stage stage) {
        Button pauseButton = new Button("Pausar");
        Button resumeButton = new Button("Continuar");
        pauseButton.setOnAction(e -> paused = true);
        resumeButton.setOnAction(e -> paused = false);

        Button slowerButton = new Button("- Tiempo");
        Button fasterButton = new Button("+ Tiempo");
        slowerButton.setOnAction(e -> adjustTimeScale(0.5));
        fasterButton.setOnAction(e -> adjustTimeScale(2.0));

        timeLabel = new Label("Tiempo: 0 s");
        timeLabel.setTextFill(Color.WHITE);
        timeLabel.setFont(Font.font(16));

        speedLabel = new Label("Velocidad x1.0");
        speedLabel.setTextFill(Color.WHITE);
        speedLabel.setFont(Font.font(16));

        Button configButton = new Button("Configuración General");
        configButton.setOnAction(e -> openConfigurationDialog(stage));

        HBox topBar = new HBox(10, pauseButton, resumeButton, slowerButton, fasterButton, timeLabel, speedLabel, configButton);
        topBar.setPadding(new Insets(10));
        topBar.setBackground(new Background(new BackgroundFill(Color.rgb(16, 33, 55), CornerRadii.EMPTY, Insets.EMPTY)));
        topBar.getChildren().forEach(node -> {
            if (node instanceof Button btn) {
                btn.setStyle("-fx-background-color: #1b4d89; -fx-text-fill: white; -fx-font-weight: bold;");
            }
        });
        return topBar;
    }

    private void adjustTimeScale(double factor) {
        timeScale = Math.max(0.125, Math.min(64, timeScale * factor));
        speedLabel.setText(String.format("Velocidad x%.2f", timeScale));
    }

    private void openConfigurationDialog(Stage owner) {
        Dialog<String> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Configuración de la fuerza gravitatoria");

        Label infoLabel = new Label("Formula actual (use variables G, m1, m2, r):");
        infoLabel.setWrapText(true);
        TextArea formulaArea = new TextArea(gravitationalFormula);
        formulaArea.setPrefRowCount(4);

        VBox content = new VBox(10, infoLabel, formulaArea);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        ButtonType applyType = new ButtonType("Aplicar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(applyType, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == applyType) {
                return formulaArea.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(formula -> {
            try {
                Expression newExpression = new ExpressionBuilder(formula)
                        .variables("G", "m1", "m2", "r")
                        .build();
                gravitationalFormula = formula;
                gravitationalExpression = newExpression;
            } catch (Exception ex) {
                showError("Formula no válida", "No se pudo interpretar la formula. Revise que solo use G, m1, m2, r.");
            }
        });
    }

    private void rebuildGravitationalExpression() {
        gravitationalExpression = new ExpressionBuilder(gravitationalFormula)
                .variables("G", "m1", "m2", "r")
                .build();
    }

    private void initPlanets() {
        planets.clear();
        trailLayer.getChildren().clear();

        planets.add(new Planet("Mercurio", 3.3011e23, 5.79e10, 47.36e3, Color.SILVER));
        planets.add(new Planet("Venus", 4.8675e24, 1.082e11, 35.02e3, Color.BEIGE));
        planets.add(new Planet("Tierra", 5.97237e24, 1.496e11, 29.78e3, Color.ROYALBLUE));
        planets.add(new Planet("Marte", 6.4171e23, 2.279e11, 24.077e3, Color.ORANGERED));
        planets.add(new Planet("Júpiter", 1.8982e27, 7.785e11, 13.07e3, Color.SANDYBROWN));
        planets.add(new Planet("Saturno", 5.6834e26, 1.433e12, 9.68e3, Color.KHAKI));
        planets.add(new Planet("Urano", 8.6810e25, 2.877e12, 6.8e3, Color.LIGHTBLUE));
        planets.add(new Planet("Neptuno", 1.02413e26, 4.503e12, 5.43e3, Color.DARKBLUE));

        for (Planet planet : planets) {
            world.getChildren().add(planet.sphere);
            trailLayer.getChildren().add(planet.trailGroup);
        }
    }

    private void initAnimationTimer() {
        timer = new AnimationTimer() {
            private long lastUpdate = -1;

            @Override
            public void handle(long now) {
                if (lastUpdate < 0) {
                    lastUpdate = now;
                    return;
                }

                double deltaSeconds = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;
                if (!paused) {
                    double scaledDelta = deltaSeconds * timeScale;
                    simulationTimeSeconds += scaledDelta;
                    updateSimulation(scaledDelta);
                    updateTimeLabel();
                }
            }
        };
        timer.start();
    }

    private void updateSimulation(double deltaSeconds) {
        for (Planet planet : planets) {
            planet.update(deltaSeconds);
        }
    }

    private void updateTimeLabel() {
        DecimalFormat format = new DecimalFormat("#,###");
        timeLabel.setText(String.format("Tiempo: %s s", format.format((long) simulationTimeSeconds)));
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private class Planet {
        private static final double TRAIL_POINT_RADIUS = 0.6;
        private final Sphere sphere;
        private final PhongMaterial material;
        private final Group trailGroup = new Group();

        private String name;
        private double mass; // kg
        private Point3D position; // metres
        private Point3D velocity; // metres per second

        Planet(String name, double mass, double orbitalRadius, double orbitalSpeed, Color color) {
            this.name = name;
            this.mass = mass;
            this.position = new Point3D(orbitalRadius, 0, 0);
            this.velocity = new Point3D(0, 0, orbitalSpeed);

            this.sphere = new Sphere(4);
            this.material = new PhongMaterial(color);
            this.sphere.setMaterial(material);
            this.sphere.setUserData(this);
            updateSpherePosition();
            addTrailPoint();

            sphere.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    selectedPlanet.set(this);
                    event.consume();
                }
            });
        }

        void update(double deltaSeconds) {
            double distance = position.magnitude();
            if (distance == 0) {
                return;
            }
            Expression expr = gravitationalExpression.setVariable("G", DEFAULT_GRAVITATIONAL_CONSTANT)
                    .setVariable("m1", SUN_MASS)
                    .setVariable("m2", mass)
                    .setVariable("r", distance);
            double force;
            try {
                force = expr.evaluate();
            } catch (ArithmeticException ex) {
                force = 0;
            }
            if (!Double.isFinite(force)) {
                force = 0;
            }
            double accelerationMagnitude = force / mass;
            Point3D directionToSun = position.multiply(-1).normalize();
            Point3D acceleration = directionToSun.multiply(accelerationMagnitude);

            velocity = velocity.add(acceleration.multiply(deltaSeconds));
            position = position.add(velocity.multiply(deltaSeconds));

            updateSpherePosition();
            maybeAddTrailPoint();
        }

        private void updateSpherePosition() {
            sphere.setTranslateX(position.getX() / DISTANCE_SCALE);
            sphere.setTranslateY(position.getY() / DISTANCE_SCALE);
            sphere.setTranslateZ(position.getZ() / DISTANCE_SCALE);
        }

        private void maybeAddTrailPoint() {
            Point3D lastPoint = null;
            if (!trailGroup.getChildren().isEmpty()) {
                Node lastNode = trailGroup.getChildren().get(trailGroup.getChildren().size() - 1);
                lastPoint = new Point3D(lastNode.getTranslateX(), lastNode.getTranslateY(), lastNode.getTranslateZ());
            }
            Point3D currentPoint = new Point3D(
                    sphere.getTranslateX(),
                    sphere.getTranslateY(),
                    sphere.getTranslateZ()
            );
            if (lastPoint == null || lastPoint.distance(currentPoint) > TRAIL_POINT_DISTANCE) {
                addTrailPoint();
            }
        }

        private void addTrailPoint() {
            Sphere trailPoint = new Sphere(TRAIL_POINT_RADIUS);
            trailPoint.setMaterial(new PhongMaterial(material.getDiffuseColor().deriveColor(0, 1, 1, 0.6)));
            trailPoint.setTranslateX(sphere.getTranslateX());
            trailPoint.setTranslateY(sphere.getTranslateY());
            trailPoint.setTranslateZ(sphere.getTranslateZ());
            trailGroup.getChildren().add(trailPoint);
            if (trailGroup.getChildren().size() > 800) {
                trailGroup.getChildren().remove(0);
            }
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getMass() {
            return mass;
        }

        public void setMass(double mass) {
            this.mass = mass;
        }

        public double getDistanceKm() {
            return position.magnitude() / 1000.0;
        }

        public void setDistanceKm(double distanceKm) {
            double distanceMetres = distanceKm * 1000.0;
            Point3D direction = position.magnitude() == 0 ? new Point3D(1, 0, 0) : position.normalize();
            position = direction.multiply(distanceMetres);
            updateSpherePosition();
            trailGroup.getChildren().clear();
            addTrailPoint();
        }
    }

    private class PlanetInfoPanel {
        private final VBox view;
        private final Label titleLabel = new Label("Seleccione un planeta");
        private final TextField nameField = new TextField();
        private final TextField massField = new TextField();
        private final TextField distanceField = new TextField();
        private final Label errorLabel = new Label();
        private final Button applyButton = new Button("Guardar cambios");

        PlanetInfoPanel() {
            titleLabel.setTextFill(Color.WHITE);
            titleLabel.setFont(Font.font(18));

            configureTextField(nameField, "Nombre");
            configureTextField(massField, "Masa (kg)");
            configureTextField(distanceField, "Distancia al Sol (km)");

            errorLabel.setTextFill(Color.ORANGERED);
            errorLabel.setWrapText(true);

            applyButton.disableProperty().bind(selectedPlanet.isNull());
            applyButton.setStyle("-fx-background-color: #2364aa; -fx-text-fill: white;");
            applyButton.setOnAction(e -> applyChanges());

            view = new VBox(8,
                    titleLabel,
                    labelledBox("Nombre", nameField),
                    labelledBox("Masa (kg)", massField),
                    labelledBox("Distancia al Sol (km)", distanceField),
                    applyButton,
                    errorLabel
            );
            view.setPadding(new Insets(15));
            view.setMinWidth(260);
            view.setStyle("-fx-background-color: rgba(12, 24, 44, 0.9);");
        }

        private void configureTextField(TextField field, String prompt) {
            field.setPromptText(prompt);
        }

        private VBox labelledBox(String label, TextField field) {
            Label lbl = new Label(label);
            lbl.setTextFill(Color.LIGHTGRAY);
            VBox box = new VBox(4, lbl, field);
            return box;
        }

        VBox getView() {
            return view;
        }

        void displayPlanet(Planet planet) {
            if (planet == null) {
                titleLabel.setText("Seleccione un planeta");
                nameField.setText("");
                massField.setText("");
                distanceField.setText("");
                errorLabel.setText("");
                errorLabel.setTextFill(Color.ORANGERED);
                return;
            }
            titleLabel.setText("Planeta: " + planet.getName());
            nameField.setText(planet.getName());
            massField.setText(String.format("%.3e", planet.getMass()));
            distanceField.setText(String.format("%.3f", planet.getDistanceKm()));
            errorLabel.setText("");
            errorLabel.setTextFill(Color.ORANGERED);
        }

        private void applyChanges() {
            Planet planet = selectedPlanet.get();
            if (planet == null) {
                return;
            }
            try {
                String name = nameField.getText().trim();
                if (!name.isEmpty()) {
                    planet.setName(name);
                }
                double mass = Double.parseDouble(massField.getText().trim());
                double distanceKm = Double.parseDouble(distanceField.getText().trim());
                if (mass <= 0 || distanceKm <= 0) {
                    throw new NumberFormatException();
                }
                planet.setMass(mass);
                planet.setDistanceKm(distanceKm);
                titleLabel.setText("Planeta: " + planet.getName());
                errorLabel.setText("Cambios aplicados");
                errorLabel.setTextFill(Color.LIGHTGREEN);
            } catch (NumberFormatException ex) {
                errorLabel.setTextFill(Color.ORANGERED);
                errorLabel.setText("Valores numéricos inválidos. Asegúrese de usar números positivos.");
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
