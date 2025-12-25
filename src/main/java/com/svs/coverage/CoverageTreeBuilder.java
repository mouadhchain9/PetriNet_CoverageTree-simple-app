package com.svs.coverage;

import com.svs.model.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.ArrayList;

public class CoverageTreeBuilder {

    private final PetriNet net;

    public CoverageTreeBuilder(PetriNet net) {
        this.net = net;
    }

    // new corrected version hopefully (x2)
    public CoverageTreeNode build(Marking initial) {

        CoverageTreeNode root =
                new CoverageTreeNode(initial, null, null);

        Deque<CoverageTreeNode> worklist = new ArrayDeque<>();
        worklist.add(root);

        while (!worklist.isEmpty()) {

            CoverageTreeNode node = worklist.removeFirst();

            // Only process NEW nodes
            if (node.getStatus() != CoverageTreeNode.Status.NEW)
                continue;

            Marking M = node.getMarking();

            // -------------------------------------------------
            // (b) identical marking on path → OLD (STOP)
            // -------------------------------------------------
            // ✅ FIX_01: this check is correct AND must be here
            if (existsIdenticalOnPath(node)) {
                node.setStatus(CoverageTreeNode.Status.OLD);
                continue;
            }
            // testing
            if (containsOmega(M)) {
                node.setStatus(CoverageTreeNode.Status.OLD);
                continue;
            }
            // Enabled transitions
            List<Transition> enabled = net.getEnabledTransitions(M);

            // (c) no enabled transitions → DEAD-END
            if (enabled.isEmpty()) {
                node.setStatus(CoverageTreeNode.Status.DEAD_END);
                continue;
            }

            // (d) expand enabled transitions (edited)
            for (Transition t : enabled) {

                Marking Mprime = t.fire(net, M);
                Mprime = applyOmegaRule(Mprime, node);

                CoverageTreeNode child =
                        new CoverageTreeNode(Mprime, node, t);

                // 🔴 CHECK STEP (b) IMMEDIATELY
                if (existsIdenticalOnPath(child)) {
                    child.setStatus(CoverageTreeNode.Status.OLD);
                    node.addChild(child);   // allowed to exist
                    continue;               // but NEVER expand
                }

                node.addChild(child);
                worklist.add(child);
            }


            // After expansion → OLD
            node.setStatus(CoverageTreeNode.Status.OLD);
        }

        return root;
    }
    // -------------------------------------------------
    // Helpers
    // -------------------------------------------------

    private boolean containsOmega(Marking m) {
        for (int i = 0; i < m.getTokens().size(); i++) {
            if (m.isOmega(i)) {
                return true;
            }
        }
        return false;
    }


    private boolean existsIdenticalOnPath(CoverageTreeNode node) {
        Marking current = node.getMarking();
        CoverageTreeNode p = node.getParent();

        while (p != null) {
            if (current.equals(p.getMarking()))
                return true;
            p = p.getParent();
        }
        return false;
    }


    private Marking applyOmegaRule(Marking Mprime,
                               CoverageTreeNode node) {

        CoverageTreeNode p = node;
        while (p != null) {
            Marking Mpp = p.getMarking();

            // if (dominates(Mprime, Mpp) && !Mprime.equals(Mpp))
            if (dominates(Mprime, Mpp)) {
                return omegaReplace(Mprime, Mpp);
            }
            p = p.getParent();
        }
        return Mprime;
    }

    private boolean dominates(Marking a, Marking b) {
        for (int i = 0; i < a.getTokens().size(); i++) {
            int x = a.getToken(i);
            int y = b.getToken(i);

            if (y == -1) continue;     // ω ≤ ω
            if (x == -1) continue;     // ω ≥ n
            if (x < y) return false;
        }
        return true;
    }

    // private Marking omegaReplace(Marking a, Marking b) {
    //     for (int i = 0; i < a.getTokens().size(); i++) {
    //         int x = a.getToken(i);
    //         int y = b.getToken(i);

    //         if (y != -1 && x > y)
    //             a.getTokens().set(i, -1);
    //     }
    //     return a;
    // }

    private Marking omegaReplace(Marking a, Marking b) {

        // ✅ FIX_02: clone tokens to avoid mutating tree markings
        List<Integer> newTokens = new ArrayList<>(a.getTokens());

        for (int i = 0; i < newTokens.size(); i++) {
            int x = newTokens.get(i);
            int y = b.getToken(i);

            if (y != -1 && x > y) {
                newTokens.set(i, -1); // ω
            }
        }

        return new Marking(newTokens, null);
    }

}

