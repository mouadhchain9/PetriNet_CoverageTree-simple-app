package com.svs.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import com.svs.model.*;

import java.util.ArrayList;
import java.util.List;

public class PopupInitMarkingController {

    @FXML private GridPane gridPlaces;
    @FXML private Button btnStart;
    @FXML private Button btnCancel;

    private PetriNet net;
    private Stage stage;

    private final List<TextField> tokenInputs = new ArrayList<>();
    private Marking resultMarking = null;

    @FXML
    private void initialize() {
        btnStart.setOnAction(e -> onStart());
        btnCancel.setOnAction(e -> onCancel());
    }

    // Called by EditorController
    public void setNet(PetriNet net) {
        this.net = net;
        populateGrid();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Marking getResultMarking() {
        return resultMarking;
    }

    // --------------------------------------------------------
    // UI: list of places + input fields
    // --------------------------------------------------------

    private void populateGrid() {
        List<Place> places = net.getPlaces();
        tokenInputs.clear();

        for (int i = 0; i < places.size(); i++) {
            Place p = places.get(i);

            Label name = new Label("Place " + p.getName() + ":");
            TextField tf = new TextField();
            tf.setPromptText("tokens");

            gridPlaces.add(name, 0, i);
            gridPlaces.add(tf, 1, i);

            tokenInputs.add(tf);
        }
    }

    // --------------------------------------------------------
    // Button handlers
    // --------------------------------------------------------

    private void onStart() {
        try {
            List<Integer> marking = new ArrayList<>();

            for (TextField tf : tokenInputs) {
                String txt = tf.getText().trim();

                if (txt.equalsIgnoreCase("w") || txt.equalsIgnoreCase("ω")) {
                    marking.add(-1);
                } else {
                    int val = Integer.parseInt(txt);
                    if (val < 0) throw new Exception();
                    marking.add(val);
                }
            }

            resultMarking = new Marking(marking, null);
            stage.close();

        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid marking input.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    private void onCancel() {
        resultMarking = null;
        stage.close();
    }
}
