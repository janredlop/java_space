package com.solarsim;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.text.DecimalFormat;

public class PlanetInfoPanel extends VBox {
    private final TextField nameField = new TextField();
    private final TextField massField = new TextField();
    private final TextField distanceField = new TextField();
    private final Label messageLabel = new Label();

    private Planet planet;
    private SimulationEngine engine;
    private final DecimalFormat formatter = new DecimalFormat("###,###,###,###");

    public PlanetInfoPanel() {
        setPadding(new Insets(10));
        setSpacing(8);
        setPrefWidth(260);

        getStyleClass().add("planet-info-panel");

        Label title = new Label("Planeta seleccionado");
        title.getStyleClass().add("info-title");

        Button applyButton = new Button("Aplicar cambios");
        applyButton.setOnAction(e -> applyChanges());

        getChildren().addAll(
                title,
                new Label("Nombre"),
                nameField,
                new Label("Masa (kg)"),
                massField,
                new Label("Distancia al Sol (km)"),
                distanceField,
                applyButton,
                messageLabel
        );

        setVisible(false);
    }

    public void setSelection(Planet planet, SimulationEngine engine) {
        this.planet = planet;
        this.engine = engine;
        if (planet == null) {
            setVisible(false);
            return;
        }
        nameField.setText(planet.getName());
        massField.setText(formatter.format(planet.getMass()));
        distanceField.setText(formatter.format(planet.getDistanceKm()));
        messageLabel.setText("");
        setVisible(true);
    }

    private void applyChanges() {
        if (planet == null || engine == null) {
            return;
        }
        try {
            String newName = nameField.getText().trim();
            double newMass = Double.parseDouble(massField.getText().replace(",", ""));
            double newDistance = Double.parseDouble(distanceField.getText().replace(",", ""));
            engine.updatePlanetParameters(planet, newName, newMass, newDistance);
            setSelection(planet, engine);
            messageLabel.setText("Datos actualizados");
        } catch (NumberFormatException ex) {
            messageLabel.setText("Valores numéricos inválidos");
        } catch (IllegalArgumentException ex) {
            messageLabel.setText(ex.getMessage());
        }
    }
}
