package com.svs.coverage.ui;

import com.svs.coverage.CoverageTreeNode;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class CoverageTreeViewNode extends StackPane {

    public static final double WIDTH = 120;
    public static final double HEIGHT = 50;

    private final CoverageTreeNode model;

    public CoverageTreeViewNode(CoverageTreeNode model) {
        this.model = model;

        Rectangle bg = new Rectangle(WIDTH, HEIGHT);
        bg.setArcWidth(12);
        bg.setArcHeight(12);
        bg.setStyle(
            "-fx-fill: white;" +
            "-fx-stroke: black;" +
            "-fx-stroke-width: 1.5;"
        );

        Label markingLabel = new Label(model.getMarking().toString());
        markingLabel.setStyle("-fx-font-family: monospace;");

        getChildren().addAll(bg, markingLabel);
    }

    public CoverageTreeNode getModel() {
        return model;
    }
}
