package com.svs.ui.nodes;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line; 
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;



public class ArcLine extends Group {

    private final Line line = new Line();
    private final Polygon arrowHead = new Polygon();

    private final Node sourceNode;
    private final Node targetNode;
    private int weight = 1;
    private Text weightLabel = new Text("1");

    public ArcLine(Node source, Node target) {
        this.sourceNode = source;
        this.targetNode = target;

        line.setStroke(Color.BLACK);
        line.setStrokeWidth(2);

        arrowHead.setFill(Color.BLACK);

        getChildren().addAll(line, arrowHead);

        // Initial update
        update();

        // Bind line to nodes dynamically
        source.layoutXProperty().addListener((obs, oldV, newV) -> update());
        source.layoutYProperty().addListener((obs, oldV, newV) -> update());
        target.layoutXProperty().addListener((obs, oldV, newV) -> update());
        target.layoutYProperty().addListener((obs, oldV, newV) -> update());
    }
    // added after animation function improvment
    public boolean isPreArcOf(TransitionNode t) {
        return sourceNode instanceof PlaceNode && targetNode == t;
    }

    public boolean isPostArcOf(TransitionNode t) {
        return sourceNode == t && targetNode instanceof PlaceNode;
    }

    public Node getSourceNode() {
        return sourceNode;
    }
    public Node getTargetNode() {
        return targetNode;
    }
    
    ///--
    // added for weight setting function
    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
        updateLabel();
    }
    private void updateLabel() {
        weightLabel.setText(String.valueOf(weight));
        if (!"1".equals(weightLabel.getText()))
            getChildren().add(weightLabel);

        weightLabel.setX((getStartX() + getEndX()) / 2);
        weightLabel.setY((getStartY() + getEndY()) / 2);
    }

    // ⬅ Fix: correct implementation
    public boolean connects(TransitionNode t) {
        return (sourceNode == t || targetNode == t);
    }

    // ⬅ coordinate getters
    public double getStartX() { return line.getStartX(); }
    public double getStartY() { return line.getStartY(); }
    public double getEndX()   { return line.getEndX();   }
    public double getEndY()   { return line.getEndY();   }

    public void update() {
        // Get center of source and target nodes
        double startX, startY, endX, endY;
        if(sourceNode instanceof PlaceNode) {
        startX = (sourceNode.getLayoutX() + sourceNode.getBoundsInParent().getWidth() / 2) - 25;
        startY = (sourceNode.getLayoutY() + sourceNode.getBoundsInParent().getHeight() / 2) - 25;
        endX = targetNode.getLayoutX() + targetNode.getBoundsInParent().getWidth() / 2;
        endY = targetNode.getLayoutY() + targetNode.getBoundsInParent().getHeight() / 2;
        } else {
        startX = sourceNode.getLayoutX() + sourceNode.getBoundsInParent().getWidth() / 2;
        startY = sourceNode.getLayoutY() + sourceNode.getBoundsInParent().getHeight() / 2;
        endX = (targetNode.getLayoutX() + targetNode.getBoundsInParent().getWidth() / 2) - 25;
        endY = (targetNode.getLayoutY() + targetNode.getBoundsInParent().getHeight() / 2) -25;    
        }


        line.setStartX(startX);
        line.setStartY(startY);

        line.setEndX(endX);
        line.setEndY(endY);

        weightLabel.setX((startX + endX) / 2);
        weightLabel.setY((startY + endY) / 2 - 5);

        // Arrowhead geometry
        double arrowLength = 12;
        double arrowWidth = 7;

        double dx = endX - startX;
        double dy = endY - startY;
        double angle = Math.atan2(dy, dx);

        double x1 = endX - arrowLength * Math.cos(angle - Math.PI / 6);
        double y1 = endY - arrowLength * Math.sin(angle - Math.PI / 6);

        double x2 = endX - arrowLength * Math.cos(angle + Math.PI / 6);
        double y2 = endY - arrowLength * Math.sin(angle + Math.PI / 6);

        arrowHead.getPoints().setAll(
                endX, endY,
                x1, y1,
                x2, y2
        );
    }
}
