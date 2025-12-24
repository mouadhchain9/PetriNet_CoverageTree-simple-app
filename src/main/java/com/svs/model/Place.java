package com.svs.model;

public class Place {

    private static int counter = 0;

    private String name;

    public Place() {
        this.name = "P" + counter++;
    }
    public static void resetCounter() {
        counter = 0;
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return name;
    }
}
