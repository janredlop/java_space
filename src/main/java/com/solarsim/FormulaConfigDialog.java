package com.solarsim;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;

public class FormulaConfigDialog extends Dialog<String> {
    private final TextArea formulaArea;

    public FormulaConfigDialog(String currentFormula) {
        setTitle("Configuración de fórmula");
        setHeaderText("Edita la fórmula de Newton usada para calcular la fuerza gravitatoria.");

        formulaArea = new TextArea(currentFormula);
        formulaArea.setPrefRowCount(4);

        GridPane content = new GridPane();
        content.setPadding(new Insets(10));
        content.setHgap(10);
        content.setVgap(10);

        Label help = new Label("Variables disponibles: G, m1, m2, r. Ejemplo: G * m1 * m2 / pow(r, 2).");
        help.setWrapText(true);

        content.add(help, 0, 0);
        content.add(formulaArea, 0, 1);

        getDialogPane().setContent(content);
        getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return formulaArea.getText();
            }
            return null;
        });
    }
}
