package com.weirdgeeks.model;

import com.jme3.scene.Spatial;

import java.util.ArrayList;
import java.util.List;

public class Peg {
    private final int number;
    private List<Disc> discs = new ArrayList<>();
    private final Spatial spatial;

    public Peg(Integer number, Spatial spatial) {
        this.number = number;
        this.spatial = spatial;
    }

    public int getNumber() {
        return number;
    }

    public List<Disc> getDiscs() {
        return discs;
    }

    public Spatial getSpatial() {
        return spatial;
    }
}
