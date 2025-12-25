package com.svs.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ArcWeightPopupController {

    @FXML private TextField weightField;
    @FXML private Label errorLabel;

    private Stage stage;
    private Integer result = null;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Integer getResult() {
        return result;
    }

    @FXML
    private void onSet() {
        try {
            int w = Integer.parseInt(weightField.getText());
            if (w <= 0) throw new NumberFormatException();

            result = w;
            stage.close();
        } catch (NumberFormatException e) {
            errorLabel.setText("Weight must be a positive integer");
        }
    }

    @FXML
    private void onCancel() {
        result = null;
        stage.close();
    }
}
