package com.svs.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PetriNet {

    private final List<Place> places = new ArrayList<>();
    private final List<Transition> transitions = new ArrayList<>();

    public void addPlace(Place p) { places.add(p); }
    public void addTransition(Transition t) { transitions.add(t); }

    public List<Place> getPlaces() { return places; }
    public List<Transition> getTransitions() { return transitions; }

    // HELPERS
    public int indexOf(Place p) { return places.indexOf(p); }

    // Enabled transitions for a marking
    public List<Transition> getEnabledTransitions(Marking m) {
        List<Transition> list = new ArrayList<>();
        for (Transition t : transitions)
            if (t.isEnabled(this, m))
                list.add(t);
        return list;
    }

    // PRINT MARKING
    public void printMarking(Marking m) {
        System.out.print("M = [ ");
        for (int i = 0; i < places.size(); i++) {
            int v = m.getToken(i);
            System.out.print(places.get(i).getName() + "=" + (v == -1 ? "ω" : v) + " ");
        }
        System.out.println("]");
    }

    // SIMULATION WITH MARKING (edited)
    // public void runInteractiveSimulation(Marking m0) {
    //     Scanner sc = new Scanner(System.in);

    //     Marking current = m0;

    //     while (true) {
    //         printMarking(current);

    //         List<Transition> enabled = getEnabledTransitions(current);
    //         if (enabled.isEmpty()) {
    //             System.out.println("No enabled transitions – simulation ends.");
    //             break;
    //         }

    //         if (enabled.size() == 1) {
    //             Transition t = enabled.get(0);
    //             System.out.println("Enabled: " + t + " (press Enter to fire)");
    //             sc.nextLine();
    //             current = t.fire(this, current);
    //         } else {
    //             System.out.println("Enabled transitions:");
    //             for (int i = 0; i < enabled.size(); i++)
    //                 System.out.println((i + 1) + ". " + enabled.get(i));

    //             System.out.print("Choose transition to fire: ");
    //             int choice = Integer.parseInt(sc.nextLine());
    //             Transition chosen = enabled.get(choice - 1);
    //             current = chosen.fire(this, current);
    //         }

    //         System.out.println("--------------------------");
    //     }
    // }
    public void runInteractiveSimulation(Marking m0) {
        Scanner sc = new Scanner(System.in);
        Marking current = m0;

        while (true) {
            printMarking(current);
            
            List<Transition> enabled = getEnabledTransitions(current);
            if (enabled.isEmpty()) {
                System.out.println("No enabled transitions – simulation ends.");
                break;
            }

            if (enabled.size() == 1) {
                Transition t = enabled.get(0);
                System.out.println("Enabled: " + t);
                System.out.println("Press Enter to fire, or type 'q' to quit: ");
                String input = sc.nextLine();
                if (input.equalsIgnoreCase("q")) {
                    System.out.println("Exiting simulation...");
                    break;
                }
                current = t.fire(this, current);
            } else {
                System.out.println("Enabled transitions:");
                for (int i = 0; i < enabled.size(); i++) {
                    System.out.println((i + 1) + ". " + enabled.get(i));
                }
                System.out.println("0. Exit simulation");
                
                System.out.print("Choose transition to fire: ");
                String input = sc.nextLine();
                
                if (input.equalsIgnoreCase("q") || input.equals("0")) {
                    System.out.println("Exiting simulation...");
                    break;
                }
                
                try {
                    int choice = Integer.parseInt(input);
                    if (choice < 1 || choice > enabled.size()) {
                        System.out.println("Invalid choice. Try again.");
                        continue;
                    }
                    Transition chosen = enabled.get(choice - 1);
                    current = chosen.fire(this, current);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number.");
                    continue;
                }
            }
            
            System.out.println("--------------------------");
        }
        
        System.out.println("Simulation ended.");
    }
    // Remove a Place/Transtion
    public void removePlace(Place p) {
        int idx = places.indexOf(p);
        if (idx == -1) return;

        // Remove arcs referencing this place
        for (Transition t : transitions) {
            t.removeArcsWith(p);
        }

        places.remove(p);
    }

    public void removeTransition(Transition t) {
        transitions.remove(t);
    }

    public void clear() {
        places.clear();
        transitions.clear();
    }

    public void resetCounters() {
        Place.resetCounter();
        Transition.resetCounter();
    }
    ////

    @Override
    public String toString() {
        return "Places: " + places + "\nTransitions: " + transitions;
    }
}
