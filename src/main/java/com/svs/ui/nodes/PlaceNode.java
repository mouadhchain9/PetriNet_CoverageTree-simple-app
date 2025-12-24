package com.svs.ui.nodes;

import com.svs.model.Place;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class PlaceNode extends Group {

    private final Place place;
    private final Circle circle;
    private final Label label;

    public static final double RADIUS = 25;

    public PlaceNode(Place place) {
        this.place = place;

        circle = new Circle(RADIUS);
        circle.setStroke(Color.BLACK);
        circle.setFill(Color.WHITE);

        label = new Label(place.getName());
        label.setTranslateX(-7);
        label.setTranslateY(-8);

        getChildren().addAll(circle, label);

        setStyle("-fx-cursor: hand;");
    }

    public Place getPlace() {
        return place;
    }

    public double getRadius() {
        return RADIUS;
    }

    public double getCenterX() {
        return getLayoutX() + RADIUS;
    }

    public double getCenterY() {
        return getLayoutY() + RADIUS;
    }
}
