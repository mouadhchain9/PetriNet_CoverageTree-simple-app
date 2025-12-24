package com.svs;

import com.svs.model.*;
import java.util.List;
import java.util.Scanner;

 public class App {
    public static void main(String[] args) {

//         PetriNet net = new PetriNet();

//         // --- Places ---
//         Place p0 = new Place(1);
//         Place p1 = new Place(0);
//         Place p2 = new Place(0);
//         Place p3 = new Place(0);

//         net.addPlace(p0);
//         net.addPlace(p1);
//         net.addPlace(p2);
//         net.addPlace(p3);

//         // --- Transitions ---
//         Transition T0 = new Transition();
//         T0.addPreArc(p0, 1);
//         T0.addPostArc(p1, 2);

//         Transition T1 = new Transition();
//         T1.addPreArc(p1, 1);
//         T1.addPostArc(p3, 1);

//         Transition T2 = new Transition();
//         T2.addPreArc(p1, 1);
//         T2.addPostArc(p2, 1);

//         net.addTransition(T0);
//         net.addTransition(T1);
//         net.addTransition(T2);

//           // --- Run the interactive simulation ---
//         net.runInteractiveSimulation();

        // PetriNetBuilder builder = new PetriNetBuilder();
        // builder.run(); // interactive building

        // PetriNet net = builder.getNet();
        // net.runInteractiveSimulation(); // interactive simulation
      System.out.println("=== Petri Net Simulator ===");

           // Create the builder
         PetriNetBuilder builder = new PetriNetBuilder();

           // Run the interactive building & simulation
         builder.run();

         System.out.println("Simulation ended."); 

   }
}