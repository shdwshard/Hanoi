package com.weirdgeeks.model;

public class Rules {
    public boolean canPlace(Disc disc, Peg peg) {
        if (peg.getDiscs().isEmpty()) return true;
        return peg.getDiscs().get(peg.getDiscs().size() - 1).getSize() > disc.getSize();
    }
}
