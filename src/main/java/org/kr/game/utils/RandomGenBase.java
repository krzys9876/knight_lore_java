package org.kr.game.utils;

public interface RandomGenBase {
    int getSeed1();
    int getSeed2();
    int getSeed3();

    void updateSeed1();
    void updateSeed2();
    void updateSeed3();
}
