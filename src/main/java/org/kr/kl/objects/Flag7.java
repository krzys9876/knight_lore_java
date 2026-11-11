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
public class Flag7 {
    private static final int nearArchBit = 0;
    private static final int ignore3DBit = 1;
    private static final int moveableBit = 2;
    private static final int autoAdjustBit = 3;
    private static final int drawBit = 4;
    private static final int wipeBit = 5;
    private static final int hFlipBit = 6;
    private static final int vFlipBit = 7;

    //private static int set()
    public boolean isNearArch(int b) { return DataBlock.isSetS(nearArchBit, b); }
}
