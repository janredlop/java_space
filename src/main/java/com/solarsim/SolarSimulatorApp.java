package com.solarsim;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.AmbientLight;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.time.Duration;
import java.util.Locale;

public class SolarSimulatorApp extends Application {
    private SimulationEngine engine;
    private AnimationTimer timer;
    private boolean paused = false;
    private long lastUpdate = 0;

    private Label timeLabel;
    private Label speedLabel;
    private PlanetInfoPanel infoPanel;

    @Override
    public void start(Stage stage) {
        engine = new SimulationEngine();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #020b2d, #000000);");

        infoPanel = new PlanetInfoPanel();
        root.setRight(infoPanel);

        SubScene subScene = new SubScene(engine.getWorld(), 1024, 720, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.rgb(3, 12, 32));
        AmbientLight ambientLight = new AmbientLight(Color.color(0.4, 0.4, 0.4));
        engine.getWorld().getChildren().add(ambientLight);
        new CameraController(subScene);

        attachPlanetSelection();

        StackPane centerPane = new StackPane(subScene);
        root.setCenter(centerPane);
        subScene.widthProperty().bind(centerPane.widthProperty());
        subScene.heightProperty().bind(centerPane.heightProperty());
        subScene.setOnMouseClicked(event -> {
            if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                if (event.getPickResult().getIntersectedNode() == null) {
                    infoPanel.setSelection(null, engine);
                }
            }
        });

        HBox topBar = createTopBar();
        root.setTop(topBar);

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setTitle("Simulador del Sistema Solar");
        stage.setScene(scene);
        stage.show();

        startAnimation();
    }

    private HBox createTopBar() {
        HBox bar = new HBox(10);
        bar.setPadding(new Insets(10));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("toolbar");

        Button pauseButton = new Button("Pausar");
        Button continueButton = new Button("Continuar");
        pauseButton.setOnAction(e -> paused = true);
        continueButton.setOnAction(e -> {
            paused = false;
            lastUpdate = 0;
        });

        Button slowerButton = new Button("- velocidad");
        Button fasterButton = new Button("+ velocidad");
        slowerButton.setOnAction(e -> changeSpeed(0.5));
        fasterButton.setOnAction(e -> changeSpeed(2.0));

        timeLabel = new Label();
        timeLabel.setFont(Font.font("Monospaced", 16));
        updateTimeLabel();

        speedLabel = new Label();
        speedLabel.setFont(Font.font("Monospaced", 14));
        updateSpeedLabel();

        Button configButton = new Button("Configuración");
        configButton.setOnAction(e -> openFormulaDialog());

        bar.getChildren().addAll(pauseButton, continueButton, new Label("Tiempo:"), timeLabel,
                slowerButton, fasterButton, speedLabel, configButton);
        return bar;
    }

    private void changeSpeed(double factor) {
        engine.setTimeScale(engine.getTimeScale() * factor);
        updateSpeedLabel();
    }

    private void updateSpeedLabel() {
        speedLabel.setText(String.format(Locale.getDefault(), "Velocidad: %.2fx", engine.getTimeScale()));
    }

    private void openFormulaDialog() {
        FormulaConfigDialog dialog = new FormulaConfigDialog(engine.getFormulaExpression());
        dialog.showAndWait().ifPresent(newFormula -> {
            try {
                engine.setFormulaExpression(newFormula);
            } catch (Exception ex) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error al aplicar fórmula");
                alert.setHeaderText("No se pudo interpretar la fórmula");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            }
        });
    }

    private void attachPlanetSelection() {
        for (Planet planet : engine.getPlanets()) {
            planet.getSphere().setOnMouseClicked(event -> {
                if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                    infoPanel.setSelection(planet, engine);
                }
            });
        }
    }

    private void startAnimation() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (paused) {
                    lastUpdate = now;
                    return;
                }
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }
                double deltaSeconds = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;
                engine.update(deltaSeconds);
                updateTimeLabel();
            }
        };
        timer.start();
    }

    private void updateTimeLabel() {
        double seconds = engine.getElapsedTimeSeconds();
        Duration duration = Duration.ofMillis((long) (seconds * 1000));
        long days = duration.toDays();
        long hours = duration.minusDays(days).toHours();
        long minutes = duration.minusDays(days).minusHours(hours).toMinutes();
        long secs = duration.minusDays(days).minusHours(hours).minusMinutes(minutes).getSeconds();
        timeLabel.setText(String.format("%d d %02d:%02d:%02d", days, hours, minutes, secs));
    }

    public static void main(String[] args) {
        launch();
    }
}
