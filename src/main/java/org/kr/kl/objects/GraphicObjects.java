package org.kr.kl.objects;

/*; #TABLE(default,centre,:w)
          ; { =h Byte(n) | =h Description }
00        ; { +0     | graphic_no. }
01        ; { +1     | x (center) }
02        ; { +2     | y (center) }
03        ; { +3     | z (bottom) }
04        ; { +4     | width (X radius) }
05        ; { +5     | depth (Y radius) }
06        ; { +6     | height }
07        ; { +7     | flags }
          ; {        | - 7=vflip sprite }
          ; {        | - 6=hflip sprite }
          ; {        | - 5=wipe }
          ; {        | - 4=draw }
          ; {        | - 3=auto-adjust near arches (player only) }
          ; {        | - 2=moveable }
          ; {        | - 1=ignore in 3D calculations }
          ; {        | - 0=is near arch (player only) }
08        ; { +8     | screen }
09        ; { +9     | dX }
0A        ; { +10    | dY }
0B        ; { +11    | dZ }
0C        ; { +12    | counter and flags }
          ; {        | - 7-4=counter when entering screen }
          ; {        | - 3=jumping }
          ; {        | - 2=Z out-of-bounds }
          ; {        | - 1=Y out-of-bounds }
          ; {        | - 0=X out-of-bounds }
0D        ; { +13    | per-object info/flags }
          ; {        | - direction and counters for looking, turning }
          ; {        | - 7=deadly if object hits player }
          ; {        | - 6=dead }
          ; {        | - 5=deadly if player hits object }
          ; {        | - 4=(not used) }
          ; {        | - 3=triggered (dropping, collapsing blocks) }
          ; {        | - 2=up (bouncing ball), dropping (spiked ball) }
          ; {        | - 1=north (NS fire) }
          ; {        | - 0=east (WE fire, EW guard), just dropped (spec objs) }
0E        ; { +14    | d_x_adj }
0F        ; { +15    | d_y_adj }
10-11     ; { +16-17 | ptr object table entry or tmp player graphic_no }
12        ; { +18    | pixel X adjustment }
13        ; { +19    | pixel Y adjustment }
14-17     ; { +20-23 | unused }
18        ; { +24    | sprite data width (bytes) }
19        ; { +25    | sprite data height (lines) }
1A        ; { +26    | pixel X }
1B        ; { +27    | pixel Y }
1C        ; { +28    | old sprite data width (bytes) }
1D        ; { +29    | old sprite data height (lines) }
1E        ; { +30    | old pixel X }
1F        ; { +31    | old pixel Y }
*/

import org.kr.DataBlock;

public class GraphicObjects {
    public final DataBlock data;

    public int start() { return data.start; }
    public int endExcl() { return data.endExcl(); }

    public int[] getCopy() { return data.getCopy(); }

    public void reset() { data.reset(); }

    public final int size;

    public GraphicObjects(int start, int size) {
        //data = new DataBlock(0x5C08, 0x40 + 0x40 + 0x20 + 0x0460);
        this.size = size;
        data = new DataBlock(start, size * 0x20);

    }

    public void copyFrom(GraphicObjects other, int otherIx, int ix) {
        for(int i = 0; i < 0x20; i++) data.set(ix + i, other.data.getU(otherIx + i));
    }

    public void copyFrom(DataBlock other, int otherIx, int ix, int bytes) {
        for(int i = 0; i < bytes; i++) data.set(ix + i, other.getU(otherIx + i));
    }

    public void resetObject(int ix) {
        for(int i = 0; i < 0x20; i++) data.set(ix + i, 0);
    }

    public String objectInfo(int num, int ix) { return data.objectInfo(num, ix); }

    public int getGrNo(int ix) { return data.getU(ix); }
    public int getX(int ix) { return data.getU(ix + 1); }
    public int getY(int ix) { return data.getU(ix + 2); }
    public int getZ(int ix) { return data.getU(ix + 3); }
    public int getW(int ix) { return data.getU(ix + 4); }
    public int getD(int ix) { return data.getU(ix + 5); }
    public int getH(int ix) { return data.getU(ix + 6); }
    public int getDX(int ix) { return data.getS(ix + 0x09); }
    public int getDXu(int ix) { return data.getU(ix + 0x09); }
    public int getDY(int ix) { return data.getS(ix + 0x0A); }
    public int getDYu(int ix) { return data.getU(ix + 0x0A); }
    public int getDZ(int ix) { return data.getS(ix + 0x0B); }
    public int getDZu(int ix) { return data.getU(ix + 0x0B); }
    public int getDXAdj(int ix) { return data.getS(ix + 0x0E); }
    public int getDYAdj(int ix) { return data.getS(ix + 0x0F); }
    public int getTemp10(int ix) { return data.getU(ix + 0x10); }
    public int getTemp11(int ix) { return data.getU(ix + 0x11); }
    public int getXAdj(int ix) { return data.getS(ix + 0x12); }
    public int getYAdj(int ix) { return data.getS(ix + 0x13); }
    public int getScreen(int ix) { return data.getU(ix + 0x08); }
    public int getPixelX(int ix) { return data.getU(ix + 0x1A); }
    public int getPixelY(int ix) { return data.getU(ix + 0x1B); }
    public int getSpriteWidthB(int ix) { return data.getS(ix + 0x18); }
    public int getSpriteHeight(int ix) { return data.getS(ix + 0x19); }
    public boolean isJumping(int ix) { return data.isSet(3, ix + 0x0C); }
    public boolean isDropping(int ix) { return data.isSet(2, ix + 0x0D); }
    public boolean isDirUp(int ix) { return data.isSet(2, ix + 0x0D); }
    public boolean isMoving(int ix) { return getDX(ix)!=0 || getDY(ix)!=0 || getDZ(ix)!=0; }
    public boolean isBitSetGrNo(int ix, int bit) { return data.isSet(bit, ix); }


    public void setGrNo(int ix, int grNo) { data.set(ix, grNo); }
    public void setX(int ix, int x) { data.set(ix + 1, x); }
    public void setY(int ix, int y) { data.set(ix + 2, y); }
    public void setZ(int ix, int z) { data.set(ix + 3, z); }
    public void setW(int ix, int w) { data.set(ix + 4, w); }
    public void setD(int ix, int d) { data.set(ix + 5, d); }
    public void setH(int ix, int h) { data.set(ix + 6, h); }
    public void setScreen(int ix, int h) { data.set(ix + 8, h); }
    public void setDX(int ix, int dx) { data.set(ix + 0x09, dx); }
    public void setDY(int ix, int dy) { data.set(ix + 0x0A, dy); }
    public void setDZ(int ix, int dz) { data.set(ix + 0x0B, dz); }
    public void setDXAdj(int ix, int dxAdj) { data.set(ix + 0x0E, dxAdj); }
    public void setDYAdj(int ix, int dyAdj) { data.set(ix + 0x0F, dyAdj); }
    public void setXAdj(int ix, int xAdj) { data.set(ix + 0x12, xAdj); }
    public void setYAdj(int ix, int yAdj) { data.set(ix + 0x13, yAdj); }
    public void setXYAdj(int ix, int xAdj, int yAdj) {
        setXAdj(ix, xAdj);
        setYAdj(ix, yAdj);
    }
    public void setSpriteWidthB(int ix, int w) { data.set(ix + 0x18, w); }
    public void setSpriteHeight(int ix, int h) { data.set(ix + 0x19, h); }

    public void setDeadlyIfObjectHitsPlayer(int ix) { data.setBit(7, ix + 0x0D); }
    //public void setDeadlyIfPlayerHitsObject(int ix) { data.setBit(5, ix + 0x0D); }
    public void setBothDeadlyFlags(int ix) { data.set(ix + 0x0D, data.getU(ix + 0x0D) | 0xA0); }
    public void setDead(int ix) { data.setBit(6, ix + 0x0D); }
    public void resetDead(int ix) { data.resetBit(6, ix + 0x0D); }
    public void setDeadOtherHalf(int ix) { setDead(ix + 0x20); }
    public void setTemp10(int ix, int temp10) { data.set(ix + 0x10, temp10); }
    public void setTemp11(int ix, int temp11) { data.set(ix + 0x11, temp11); }
    public void setRising(int ix) { data.resetBit(2, ix + 0x0D); }
    public void setDropping(int ix) { data.setBit(2, ix + 0x0D); }
    public void setDirUp(int ix) { data.setBit(2, ix + 0x0D); }
    public void setDirDown(int ix) { data.resetBit(2, ix + 0x0D); }
    public void toggleNextPrevSprite(int ix) { setGrNo(ix, getGrNo(ix) ^ 0x01); }
    public void setTriggered(int ix) { data.setBit(3, ix + 0x0D); }
    public void resetTriggered(int ix) { data.resetBit(3, ix + 0x0D); }
    public void clearOob(int ix) { data.set(ix + 0x0C, getFlagsC(ix) & 0xF8); }
    public void setJustDropped(int ix) { data.setBit(0, ix + 0x0D); }
    public void resetJustDropped(int ix) { data.resetBit(0, ix + 0x0D); }
    public void toggleEastWest(int ix) { data.set(ix + 0x0D, (data.getU(ix + 0x0D) ^ 0x01)); }
    public void setPixelX(int ix, int x) { data.set(ix + 0x1A, x); }
    public void setPixelY(int ix, int y) { data.set(ix + 0x1B, y); }
    public void setOldSpriteWidthB(int ix, int w) { data.set(ix + 0x1C, w); }
    public void setOldSpriteHeight(int ix, int h) { data.set(ix + 0x1D, h); }
    public void setOldPixelX(int ix, int x) { data.set(ix + 0x1E, x); }
    public void setOldPixelY(int ix, int y) { data.set(ix + 0x1F, y); }
    public void setJumping(int ix) { data.setBit(3, ix + 0x0C); }
    public void resetJumping(int ix) { data.resetBit(3, ix + 0x0C); }

    public void setBitGrNo(int ix, int bit) { data.setBit(bit, ix); }
    public void resetBitGrNo(int ix, int bit) { data.resetBit(bit, ix); }
    public void toggleBitGrNo(int ix, int bit) { data.toggleBit(bit, ix); }

    // Flag 7
    public int getFlags7(int ix) { return data.getS(ix + 7); }
    public void setFlags7(int ix, int f) { data.set(ix + 7, f); }

    public boolean isNearArch(int ix) { return Flags7.isNearArch(getFlags7(ix)); }
    public void setNearArch(int ix) { setFlags7(ix, Flags7.setNearArch(getFlags7(ix))); }
    public void resetNearArch(int ix) { setFlags7(ix, Flags7.resetNearArch(getFlags7(ix))); }

    public boolean ignore3D(int ix) { return Flags7.ignore3D(getFlags7(ix)); }
    public void setIgnore3D(int ix) { setFlags7(ix, Flags7.setIgnore3D(getFlags7(ix))); }
    public void resetIgnore3D(int ix) { setFlags7(ix, Flags7.resetIgnore3D(getFlags7(ix))); }

    public boolean isMoveable(int ix) { return Flags7.isMoveable(getFlags7(ix)); }

    public boolean isAutoAdjust(int ix) { return Flags7.isAutoAdjust(getFlags7(ix)); }

    public void setDraw(int ix) { setFlags7(ix, Flags7.setDraw(getFlags7(ix))); }

    public void setWipe(int ix) { setFlags7(ix, Flags7.setWipe(getFlags7(ix))); }
    public void resetWipe(int ix) { setFlags7(ix, Flags7.resetWipe(getFlags7(ix))); }

    public boolean isHFlip(int ix) { return Flags7.isHFlip(getFlags7(ix)); }
    public void setHFlip(int ix) { setFlags7(ix, Flags7.setHFlip(getFlags7(ix))); }
    public void resetHFlip(int ix) { setFlags7(ix, Flags7.resetHFlip(getFlags7(ix))); }
    public void toggleHFlip(int ix) { setFlags7(ix, Flags7.toggleHFlip(getFlags7(ix))); }

    public boolean isVFlip(int ix) { return Flags7.isVFlip(getFlags7(ix)); }

    // Flags C
    public int getFlagsC(int ix) { return data.getS(ix + 0x0C); }
    public void setFlagsC(int ix, int f) { data.set(ix + 0x0C, f); }

    public int getEnterCounter(int ix) { return getFlagsC(ix) & 0xF0; }
    public boolean enteringScreen(int ix) { return getEnterCounter(ix) > 0; }
    public void initEnterCounter(int ix) { setFlagsC(ix, getFlagsC(ix) | 0x30); }
    public void decEnterCounter(int ix) {
        int counter = getEnterCounter(ix);
        if(counter >= 0x10) setFlagsC(ix, (getFlagsC(ix) & 0x0F) + counter - 0x10);
    }

    public boolean isOobX(int ix) { return data.isSet(0, ix + 0x0C); }
    public boolean isOobY(int ix) { return data.isSet(1, ix + 0x0C); }
    public boolean isOobXY(int ix) { return isOobX(ix) || isOobY(ix); }
    public boolean isOobZ(int ix) { return data.isSet(2, ix + 0x0C); }

    public void setOobX(int ix) { data.setBit(0, ix + 0x0C); }
    public void setOobY(int ix) { data.setBit(1, ix + 0x0C); }
    public void setOobZ(int ix) { data.setBit(2, ix + 0x0C); }


    // Flags D
    public int getFlagsD(int ix) { return data.getS(ix + 0x0D); }
    public void setFlagsD(int ix, int f) { data.set(ix + 0x0D, f); }

    public boolean isDead(int ix) { return data.isSet(6, ix + 0x0D); }

    public int getTurnCounter(int ix) { return getFlagsD(ix) & 0x07; }
    public void initTurnCounter(int ix) { setFlagsD(ix, 2); }
    public void decTurnCounter(int ix) {
        int counter = getTurnCounter(ix);
        if(counter >= 1) setFlagsD(ix, (getFlagsD(ix) & 0xF8) + counter - 1);
    }

    public boolean getDeadlyIfObjectHitsPlayer(int ix) { return data.isSet(7, ix + 0x0D); }
    public boolean getDeadlyIfPlayerHitsObject(int ix) { return data.isSet(5, ix + 0x0D); }

    public boolean isTriggered(int ix) { return data.isSet(3, ix + 0x0D); }
    public boolean justDropped(int ix) { return data.isSet(0, ix + 0x0D); }

    public boolean isEast(int ix) { return data.isSet(0, ix + 0x0D); }
    public boolean isNorth(int ix) { return data.isSet(1, ix + 0x0D); }
    public int getDirection(int ix) { return getFlagsD(ix) & 0x03; }
    public void incDirection(int ix) { setFlagsD(ix, getFlagsD(ix) & 0xFC | ((getDirection(ix) + 1) & 0x03)); }

}
