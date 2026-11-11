package org.kr.kl.objects;

import org.kr.DataBlock;

/*
07        ; { +7     | flags }
          ; {        | - 7=vflip sprite }
          ; {        | - 6=hflip sprite }
          ; {        | - 5=wipe }
          ; {        | - 4=draw }
          ; {        | - 3=auto-adjust near arches (player only) }
          ; {        | - 2=moveable }
          ; {        | - 1=ignore in 3D calculations }
          ; {        | - 0=is near arch (player only) }

 */
public class Flags7 {
    private static final int nearArchBit = 0;
    private static final int ignore3DBit = 1;
    private static final int moveableBit = 2;
    private static final int autoAdjustBit = 3;
    private static final int drawBit = 4;
    private static final int wipeBit = 5;
    private static final int hFlipBit = 6;
    private static final int vFlipBit = 7;

    public static boolean isNearArch(int b) { return DataBlock.isSetS(nearArchBit, b); }
    public static int setNearArch(int b) { return DataBlock.getWithBitSetS(nearArchBit, b); }
    public static int resetNearArch(int b) { return DataBlock.getWithBitResetS(nearArchBit, b); }

    public static boolean ignore3D(int b) { return DataBlock.isSetS(ignore3DBit, b); }
    public static int setIgnore3D(int b) { return DataBlock.getWithBitSetS(ignore3DBit, b); }
    public static int resetIgnore3D(int b) { return DataBlock.getWithBitResetS(ignore3DBit, b); }

    public static boolean isMoveable(int b) { return DataBlock.isSetS(moveableBit, b); }
    public static int setMoveable(int b) { return DataBlock.getWithBitSetS(moveableBit, b); }
    public static int resetMoveable(int b) { return DataBlock.getWithBitResetS(moveableBit, b); }

    public static boolean isAutoAdjust(int b) { return DataBlock.isSetS(autoAdjustBit, b); }
    public static int setAutoAdjust(int b) { return DataBlock.getWithBitSetS(autoAdjustBit, b); }
    public static int resetAutoAdjust(int b) { return DataBlock.getWithBitResetS(autoAdjustBit, b); }

    public static boolean draw(int b) { return DataBlock.isSetS(drawBit, b); }
    public static int setDraw(int b) { return DataBlock.getWithBitSetS(drawBit, b); }
    public static int resetDraw(int b) { return DataBlock.getWithBitResetS(drawBit, b); }

    public static boolean wipe(int b) { return DataBlock.isSetS(wipeBit, b); }
    public static int setWipe(int b) { return DataBlock.getWithBitSetS(wipeBit, b); }
    public static int resetWipe(int b) { return DataBlock.getWithBitResetS(wipeBit, b); }

    public static boolean isHFlip(int b) { return DataBlock.isSetS(hFlipBit, b); }
    public static int setHFlip(int b) { return DataBlock.getWithBitSetS(hFlipBit, b); }
    public static int resetHFlip(int b) { return DataBlock.getWithBitResetS(hFlipBit, b); }
    public static int flipHFlip(int b) { return DataBlock.getWithBitFlippedS(hFlipBit, b); }

    public static boolean isVFlip(int b) { return DataBlock.isSetS(vFlipBit, b); }
    public static int setVFlip(int b) { return DataBlock.getWithBitSetS(vFlipBit, b); }
    public static int resetVFlip(int b) { return DataBlock.getWithBitResetS(vFlipBit, b); }
}
