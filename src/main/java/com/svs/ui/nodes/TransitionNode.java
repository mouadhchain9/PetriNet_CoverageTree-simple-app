package com.svs.ui.nodes;

import com.svs.model.Transition;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class TransitionNode extends Group {

    private final Transition transition;
    private final Rectangle rect;
    private final Label label;

    public static final double WIDTH = 40;
    public static final double HEIGHT = 40;

    public TransitionNode(Transition transition) {
        this.transition = transition;

        rect = new Rectangle(WIDTH, HEIGHT);
        rect.setStroke(Color.BLACK);
        rect.setFill(Color.LIGHTGRAY);

        label = new Label(transition.getName());
        label.setTranslateX(WIDTH/2 - 7);
        label.setTranslateY(HEIGHT / 2 - 8);

        getChildren().addAll(rect, label);

        setStyle("-fx-cursor: hand;");
    }

    public Transition getTransition() {
        return transition;
    }

    public double getCenterX() {
        return getLayoutX() + WIDTH / 2.0;
    }

    public double getCenterY() {
        return getLayoutY() + HEIGHT / 2.0;
    }

    // TransitionNode.java
    public void highlight(boolean on) {
        if (on)
            rect.setFill(Color.LIGHTGREEN);
        else
            rect.setFill(Color.LIGHTGRAY);
    }

}
