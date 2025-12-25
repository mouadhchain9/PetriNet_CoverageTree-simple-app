package com.svs.coverage;

public class CoverageTreePrinter {

    public static void print(CoverageTreeNode root) {
        printRec(root, 0);
    }

    private static void printRec(CoverageTreeNode node, int depth) {

        String indent = "  ".repeat(depth);

        String label = node.getIncomingTransition() == null
                ? "ROOT"
                : node.getIncomingTransition().getName();

        System.out.println(
            indent + label + " → " +
            node.getMarking() +
            " [" + node.getStatus() + "]"
        );

        for (CoverageTreeNode child : node.getChildren())
            printRec(child, depth + 1);
    }
}
