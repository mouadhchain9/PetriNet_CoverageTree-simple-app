package com.svs.coverage;

import com.svs.model.Marking;
import com.svs.model.Transition;

import java.util.ArrayList;
import java.util.List;

public class CoverageTreeNode {

    private final Marking marking;
    private final CoverageTreeNode parent;
    private final Transition incomingTransition;
    private final List<CoverageTreeNode> children = new ArrayList<>();

    private Status status = Status.NEW;

    public enum Status {
        NEW, OLD, DEAD_END
    }

    public CoverageTreeNode(Marking marking,
                            CoverageTreeNode parent,
                            Transition incomingTransition) {
        this.marking = marking;
        this.parent = parent;
        this.incomingTransition = incomingTransition;
    }

    public Marking getMarking() { return marking; }
    public CoverageTreeNode getParent() { return parent; }
    public Transition getIncomingTransition() { return incomingTransition; }
    public List<CoverageTreeNode> getChildren() { return children; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public void addChild(CoverageTreeNode child) {
        children.add(child);
    }
}
