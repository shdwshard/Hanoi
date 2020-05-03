package com.weirdgeeks.model;

import com.jme3.scene.Spatial;

public class Disc {
    private final int size;
    private Peg location;
    private final Spatial spatial;

    public Disc(Integer size, Spatial spatial) {
        this.size = size;
        this.spatial = spatial;
    }

    public int getSize() {
        return size;
    }

    public Peg getLocation() {
        return location;
    }

    public void setLocation(Peg location) {
        this.location = location;
    }

    public Spatial getSpatial() {
        return spatial;
    }
}
