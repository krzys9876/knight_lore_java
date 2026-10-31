package org.kr.game.utils;

import java.util.Random;

public class SeedGeneratorRandom implements SeedGenerator {

    private int seed1;
    private int seed2;
    private int seed3;

    private final Random rnd;

    public SeedGeneratorRandom() {
        seed1 = 0x65; // originally taken from 5C78 (LSB of FRAMES 3-byte system variable)
        seed2 = 0;
        seed3 = 0;
        rnd = new Random();

        updateSeed3();
    }

    @Override
    public int getSeed1() {
        return seed1;
    }

    @Override
    public int getSeed2() {
        return seed2;
    }

    @Override
    public int getSeed3() {
        return seed3;
    }

    @Override
    public void updateSeed1() {
        seed1 = (seed1 + seed2) & 0xFF;
    }

    @Override
    public void updateSeed2() {
        seed2 = (seed2 + 1) & 0xFF;
    }

    @Override
    public void updateSeed3() {
        seed3 = (seed3 + rnd.nextInt(0xFF+1)) & 0xFF;
    }
}
