package org.kr.kl.objects;

import org.kr.kl.data.DataBlock;

public class FlagsC {
    public static int getEnterCounter(int b) { return b & 0xF0; }
    public static boolean enteringScreen(int b) { return getEnterCounter(b) > 0; }
    public static int initEnterCounter(int b) { return b | 0x30; }
    public static int decEnterCounter(int b) {
        int counter = getEnterCounter(b);
        return counter >= 0x10 ? (b & 0x0F) + counter - 0x10 : b;
    }

    public static boolean isOobX(int b) { return DataBlock.isSetS(0, b); }
    public static boolean isOobY(int b) { return DataBlock.isSetS(1, b); }
    public static boolean isOobXY(int b) { return isOobX(b) || isOobY(b); }
    public static boolean isOobZ(int b) { return DataBlock.isSetS(2, b); }

    public static int setOobX(int b) { return DataBlock.getWithBitSetS(0, b); }
    public static int setOobY(int b) { return DataBlock.getWithBitSetS(1, b); }
    public static int setOobZ(int b) { return DataBlock.getWithBitSetS(2, b); }
    public static int clearOob(int b) { return b & 0xF8; }

    public static boolean isJumping(int b) { return DataBlock.isSetS(3, b); }
    public static int setJumping(int b) { return DataBlock.getWithBitSetS(3, b); }
    public static int resetJumping(int b) { return DataBlock.getWithBitResetS(3, b); }

}
