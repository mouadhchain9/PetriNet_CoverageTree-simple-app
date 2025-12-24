package com.svs.model;

import java.util.ArrayList;
import java.util.List;

public class Transition {

    private static int counter = 0;

    private String name;
    private final List<Arc> pre = new ArrayList<>();
    private final List<Arc> post = new ArrayList<>();

    public Transition() {
        this.name = "T" + counter++;
    }

    public static void resetCounter() {
        counter = 0;
    }

    public void addPreArc(Place p, int w) {
        pre.add(new Arc(p, w));
    }

    public void addPostArc(Place p, int w) {
        post.add(new Arc(p, w));
    }

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }
    public List<Arc> getPre() {return this.pre; }
    public List<Arc> getPost() {return this.post; }

    // ----------------------------
    // ENABLED TEST WITH MARKING
    // ----------------------------
    public boolean isEnabled(PetriNet net, Marking m) {
        for (Arc a : pre) {
            int idx = net.indexOf(a.getPlace());
            int tokens = m.getToken(idx);

            if (tokens != -1 && tokens < a.getWeight())
                return false;
        }
        return true;
    }

    // ----------------------------
    // FIRE RETURNS A NEW MARKING
    // ----------------------------
    public Marking fire(PetriNet net, Marking m) {
        if (!isEnabled(net, m))
            throw new IllegalStateException("Transition " + name + " not enabled.");

        List<Integer> newTokens = new ArrayList<>(m.getTokens());

        // Consume
        for (Arc a : pre) {
            int idx = net.indexOf(a.getPlace());
            int val = newTokens.get(idx);

            if (val != -1)
                newTokens.set(idx, val - a.getWeight());
        }

        // Produce
        for (Arc a : post) {
            int idx = net.indexOf(a.getPlace());
            int val = newTokens.get(idx);

            if (val == -1)
                continue;

            long produced = (long) val + a.getWeight();
            if (produced > Integer.MAX_VALUE)
                newTokens.set(idx, -1); // ω
            else
                newTokens.set(idx, (int) produced);
        }

        Marking marking = new Marking(newTokens, m);

        return marking;
    }
    // delete relevant arcs when deleing a transtion
    public void removeArcsWith(Place p) {
        pre.removeIf(a -> a.getPlace().equals(p));
        post.removeIf(a -> a.getPlace().equals(p));
    }


    @Override
    public String toString() {
        return name;
    }
}
