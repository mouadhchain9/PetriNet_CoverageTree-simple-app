package com.svs.coverage.ui;

import com.svs.coverage.CoverageTreeNode;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.Map;

public class CoverageTreeRenderer {

    private static final double H_SPACING = 60;
    private static final double V_SPACING = 120;


    private final Map<CoverageTreeNode, CoverageTreeViewNode> viewMap = new HashMap<>();

    public Pane render(CoverageTreeNode root) {
        Pane pane = new Pane();

        layoutSubtree(root, 0, 0, pane);

        drawEdges(root, pane);

        return pane;
    }

    private double layoutSubtree(CoverageTreeNode node,
                                 int depth,
                                 double x,
                                 Pane pane) {

        CoverageTreeViewNode view =
                new CoverageTreeViewNode(node);

        view.setLayoutX(x);
        view.setLayoutY(depth * V_SPACING);

        pane.getChildren().add(view);
        viewMap.put(node, view);

        double childX = x;
        for (CoverageTreeNode child : node.getChildren()) {
            childX = layoutSubtree(
                child,
                depth + 1,
                childX,
                pane
            );
            childX += H_SPACING;
        }

        return Math.max(childX, x + H_SPACING);
    }

    private void drawEdges(CoverageTreeNode node, Pane pane) {
        CoverageTreeViewNode parentView = viewMap.get(node);

        for (CoverageTreeNode child : node.getChildren()) {

            CoverageTreeViewNode childView = viewMap.get(child);

            Line edge = new Line(
                parentView.getLayoutX() + CoverageTreeViewNode.WIDTH / 2,
                parentView.getLayoutY() + CoverageTreeViewNode.HEIGHT,
                childView.getLayoutX() + CoverageTreeViewNode.WIDTH / 2,
                childView.getLayoutY()
            );

            pane.getChildren().add(edge);

            if (child.getIncomingTransition() != null) {
                Text label = new Text(
                    child.getIncomingTransition().getName()
                );

                label.setX(
                    (edge.getStartX() + edge.getEndX()) / 2
                );
                label.setY(
                    (edge.getStartY() + edge.getEndY()) / 2 - 4
                );

                pane.getChildren().add(label);
            }

            drawEdges(child, pane);
        }
    }
}
