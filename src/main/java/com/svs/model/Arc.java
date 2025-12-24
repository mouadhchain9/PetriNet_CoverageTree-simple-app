package com.svs.model;


public class Arc {

    public final Place place;
    public final int weight;

    public Arc(Place place, int weight) {
        this.place = place;
        this.weight = weight;
    }


    public Place getPlace() { return place; }
    public int getWeight() { return weight; }


}
