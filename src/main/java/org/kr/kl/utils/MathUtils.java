package org.kr.kl.utils;

public class MathUtils {
    public static int byteToBCD(int value) {
        int bcd3 = (value / 100) % 10;
        value -= bcd3 * 100;
        int bcd2 = (value / 10) % 10;
        value -= bcd2 * 10;
        int bcd1 = value;
        return bcd1 + bcd2 * 16 + bcd3 * 256;
    }
}
