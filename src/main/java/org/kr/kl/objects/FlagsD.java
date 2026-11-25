package org.kr.kl.objects;

import org.kr.kl.data.DataBlock;

public class FlagsD {
    private static final int justDroppedBit = 0;
    private static final int eastWestBit = 0;
    private static final int northSouthBit = 1;
    private static final int droppingBit = 2;
    private static final int triggeredBit = 3;
    private static final int deadBit = 6;
    private static final int deadlyIfObjectHitsPlayerBit = 7;
    private static final int deadlyIfPlayerHitsObjectBit = 5;

    public static boolean isDropping(int b) { return DataBlock.isSetS(droppingBit, b); }
    public static int setRising(int b) { return DataBlock.getWithBitResetS(droppingBit, b); }
    public static int setDropping(int b) { return DataBlock.getWithBitSetS(droppingBit, b); }
    public static boolean isDirUp(int b) { return DataBlock.isSetS(droppingBit, b); }
    public static int setDirUp(int b) { return DataBlock.getWithBitSetS(droppingBit, b); }
    public static int setDirDown(int b) { return DataBlock.getWithBitResetS(droppingBit, b); }

    public static boolean justDropped(int b) { return DataBlock.isSetS(justDroppedBit, b); }
    public static int setJustDropped(int b) { return DataBlock.getWithBitSetS(justDroppedBit, b); }
    public static int resetJustDropped(int b) { return DataBlock.getWithBitResetS(justDroppedBit, b); }

    public static boolean isTriggered(int b) { return DataBlock.isSetS(triggeredBit, b); }
    public static int setTriggered(int b) { return DataBlock.getWithBitSetS(triggeredBit, b); }
    public static int resetTriggered(int b) { return DataBlock.getWithBitResetS(triggeredBit, b); }

    public static boolean isEast(int b) { return DataBlock.isSetS(eastWestBit, b); }
    public static boolean isNorth(int b) { return DataBlock.isSetS(northSouthBit, b); }
    public static int getDirection(int b) { return b & 0x03; }
    public static int  incDirection(int b) { return (b & 0xFC) | ((getDirection(b) + 1) & 0x03); }
    public static int  toggleEastWest(int b) { return b ^ (1 << eastWestBit); }
    public static int  toggleNorthSouth(int b) { return b ^ (1 << northSouthBit); }

    public static int getLookAroundCounter(int b) { return b & 0x0F; } // NOTE: multi-purpose bits
    public static int decLookAroundCounter(int b) { return (b & 0xF0) | ((getLookAroundCounter(b) - 1) & 0x0F); }
    public static int initLookAroundCounter(int b) { return (b & 0xF0) | 8; }

    public static int getTurnCounter(int b) { return b & 0x07; }
    public static int initTurnCounter(int b) { return b & 0xF8 | 2; }
    public static int decTurnCounter(int b) {
        int counter = getTurnCounter(b);
        return counter >= 1 ? (b & 0xF8) + counter - 1 : counter;
    }

    public static boolean isDead(int b) { return DataBlock.isSetS(deadBit, b); }
    public static int setDead(int b) { return DataBlock.getWithBitSetS(deadBit, b); }
    public static int resetDead(int b) { return DataBlock.getWithBitResetS(deadBit, b); }

    public static int setDeadlyIfObjectHitsPlayer(int b) { return DataBlock.getWithBitSetS(deadlyIfObjectHitsPlayerBit, b); }
    public static int setDeadlyIfPlayerHitsObject(int b) { return DataBlock.getWithBitSetS(deadlyIfPlayerHitsObjectBit, b); }
    public static int setBothDeadlyFlags(int b) { return setDeadlyIfObjectHitsPlayer(setDeadlyIfPlayerHitsObject(b)); }
    public static boolean getDeadlyIfObjectHitsPlayer(int b) { return DataBlock.isSetS(deadlyIfObjectHitsPlayerBit, b); }
    public static boolean getDeadlyIfPlayerHitsObject(int b) { return DataBlock.isSetS(deadlyIfPlayerHitsObjectBit, b); }
}
