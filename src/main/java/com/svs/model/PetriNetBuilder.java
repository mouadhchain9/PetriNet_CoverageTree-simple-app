package com.svs.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

import com.svs.coverage.*;

public class PetriNetBuilder {


    private final PetriNet net = new PetriNet();
    private final Scanner sc = new Scanner(System.in);
    private Marking initialMarking;

    public PetriNet getNet() {
        return net;
    }

    public void run() {
        System.out.println("=== Interactive Petri Net Builder ===");

        // boolean building = true; no longer needed in the while loop
        while (true) {
            System.out.println("\nOptions:");
            System.out.println("1. Add place");
            System.out.println("2. Add transition");
            System.out.println("3. Connect place to transition (pre/post)");
            System.out.println("4. Show current net");
            System.out.println("5. Finish building and start simulation");
            System.out.println("6. Print Coverage Tree");
            System.out.println("7. Drop Current Petri net");
            System.out.println("8. select initial Marking");
            System.out.print("Choose an option: ");

            int choice = readInt(1, 8);

            switch (choice) {
                case 1 -> addPlaceInteractive();
                case 2 -> addTransitionInteractive();
                case 3 -> connectPlaceTransitionInteractive();
                case 4 -> showNet();
                case 5 -> runSimulation();
                case 6 -> coverageTree();
                case 7 -> deleteAll();
                case 8 -> setMarking();

            }
        }
        // -- moved to an independant functions:
        // // ---------------------------
        // // Define initial marking
        // // ---------------------------
        // // Build a list of token counts
        // List<Integer> tokenList = new ArrayList<>();
        // for (Place p : net.getPlaces()) {
        //     System.out.print("Enter initial tokens for " + p.getName() + ": ");
        //     int tokens = readInt(0, Integer.MAX_VALUE);
        //     tokenList.add(tokens);
        // }

        // // Create marking
        // initialMarking = new Marking(tokenList, null); // null = no parent

        // ---------------------------
        // Run interactive simulation
        // ---------------------------
        // System.out.println("\n=== Starting Interactive Simulation ===");
        // net.runInteractiveSimulation(initialMarking);
    }

    private void runSimulation() {
        if (initialMarking == null) {
            System.out.println("\nyou need to set an Initial Marking first");
            return;
        }
        System.out.println("\n=== Starting Interactive Simulation ===");
        net.runInteractiveSimulation(initialMarking);
    }

    private void setMarking() {
        List<Integer> tokenList = new ArrayList<>();
        for (Place p : net.getPlaces()) {
            System.out.print("Enter initial tokens for " + p.getName() + ": ");
            int tokens = readInt(0, Integer.MAX_VALUE);
            tokenList.add(tokens);
        }
        initialMarking = new Marking(tokenList, null);
    }

    // ---------------------------
    // Place / Transition creation
    // ---------------------------
    private void addPlaceInteractive() {
        Place p = new Place(); // no tokens
        net.addPlace(p);
        System.out.println("Added place: " + p.getName());
    }

    private void addTransitionInteractive() {
        Transition t = new Transition();
        net.addTransition(t);
        System.out.println("Added transition: " + t.getName());
    }

    private void connectPlaceTransitionInteractive() {
        List<Place> places = net.getPlaces();
        List<Transition> transitions = net.getTransitions();

        if (places.isEmpty() || transitions.isEmpty()) {
            System.out.println("You need at least one place and one transition first.");
            return;
        }

        // Select place
        System.out.println("Select place:");
        for (int i = 0; i < places.size(); i++) {
            System.out.println((i + 1) + ". " + places.get(i).getName());
        }
        int placeIndex = readInt(1, places.size()) - 1;
        Place p = places.get(placeIndex);

        // Select transition
        System.out.println("Select transition:");
        for (int i = 0; i < transitions.size(); i++) {
            System.out.println((i + 1) + ". " + transitions.get(i).getName());
        }
        int transIndex = readInt(1, transitions.size()) - 1;
        Transition t = transitions.get(transIndex);

        // Pre or Post
        System.out.print("Connect as pre-arc or post-arc? (pre/post): ");
        String type = sc.nextLine().trim().toLowerCase();
        while (!type.equals("pre") && !type.equals("post")) {
            System.out.print("Invalid input. Enter 'pre' or 'post': ");
            type = sc.nextLine().trim().toLowerCase();
        }

        System.out.print("Enter weight of the arc: ");
        int weight = readInt(1, Integer.MAX_VALUE);

        if (type.equals("pre")) t.addPreArc(p, weight);
        else t.addPostArc(p, weight);

        System.out.println("Connected " + p.getName() + " -> " + t.getName() +
                " as " + type + "-arc with weight " + weight);
    }

    // ---------------------------
    // Show net
    // ---------------------------
    private void showNet() {
        System.out.println("\nCurrent Petri Net:");
        System.out.println("Places:");
        for (Place p : net.getPlaces()) {
            System.out.println("  " + p.getName());
        }
        System.out.println("Transitions:");
        for (Transition t : net.getTransitions()) {
            System.out.println("  " + t.getName());
        }
    }

    // ---------------------------
    // Coverage Tree call
    // ---------------------------

    private void coverageTree() {
        CoverageTreeBuilder builder = new CoverageTreeBuilder(net);

        CoverageTreeNode root = builder.build(initialMarking);

        CoverageTreePrinter.print(root);
    }

    private void deleteAll() {

        net.clear();
        net.resetCounters();
        System.out.println("network deleted ✔");
    }

    // ---------------------------
    // Helper to read integer in range
    // ---------------------------
    private int readInt(int min, int max) {
        int val;
        while (true) {
            String input = sc.nextLine();
            try {
                val = Integer.parseInt(input);
                if (val >= min && val <= max) break;
                else System.out.print("Out of range. Try again: ");
            } catch (NumberFormatException e) {
                System.out.print("Invalid number. Try again: ");
            }
        }
        return val;
    }
}
