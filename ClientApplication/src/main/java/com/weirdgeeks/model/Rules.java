package com.weirdgeeks.model;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class Rules {
    private Instant startTime = Instant.now();
    private Integer moves = 0;

    public boolean canPlace(Disc disc, Peg peg) {
        if (peg.getDiscs().isEmpty()) return true;
        return peg.getDiscs().get(peg.getDiscs().size() - 1).getSize() > disc.getSize();
    }
    public Duration getElapsedTime() {
        return Duration.between(startTime, Instant.now());
    }
    public Integer getMoves() {
        return moves;
    }
    public void doMove() {
        moves++;
    }

    public boolean isWin(List<Peg> pegList) {
        return (pegList.get(0).getDiscs().isEmpty() && pegList.get(1).getDiscs().isEmpty());
    }
}
