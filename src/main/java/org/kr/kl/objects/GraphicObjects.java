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

    public int start() {
        return data.start;
    }

    public int endExcl() {
        return data.endExcl();
    }

    public int[] getCopy() {
        return data.getCopy();
    }

    public final int size;

    public GraphicObjects() {
        data = new DataBlock(0x5C08, 0x40 + 0x40 + 0x20 + 0x0460);
        size = data.size / 32;
    }

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
    public int getFlags7(int ix) { return data.getS(ix + 7); }
    public int getFlagsC(int ix) { return data.getS(ix + 0x0C); }
    public int getTemp10(int ix) { return data.getU(ix + 0x10); }
    public int getTemp11(int ix) { return data.getU(ix + 0x11); }
    public int getXAdj(int ix) { return data.getS(ix + 0x12); }
    public int getYAdj(int ix) { return data.getS(ix + 0x13); }
    public int getScreen(int ix) { return data.getU(ix + 0x08); }
    public int getPixelX(int ix) { return data.getS(ix + 0x1A); }
    public int getPixelY(int ix) { return data.getS(ix + 0x1B); }
    public int getSpriteWidthB(int ix) { return data.getS(ix + 0x18); }
    public int getSpriteHeight(int ix) { return data.getS(ix + 0x19); }
    public boolean isHFlip(int ix) { return data.isSet(6, ix + 0x07); } // BIT 6,(IX+$07)
    public boolean isAutoAdjust(int ix) { return data.isSet(3, ix + 0x07); } // BIT 6,(IX+$07)
    public boolean isOobX(int ix) { return data.isSet(0, ix + 0x0C); }
    public boolean isOobY(int ix) { return data.isSet(1, ix + 0x0C); }
    public boolean isOobXY(int ix) { return isOobX(ix) || isOobY(ix); }
    public boolean isOobZ(int ix) { return data.isSet(2, ix + 0x0C); }
    public boolean isDead(int ix) { return data.isSet(6, ix + 0x0D); }
    public boolean isJumping(int ix) { return data.isSet(3, ix + 0x0C); }
    public boolean isDropping(int ix) { return data.isSet(2, ix + 0x0D); }
    public boolean isDirUp(int ix) { return data.isSet(2, ix + 0x0D); }
    public boolean isMoving(int ix) { return getDX(ix)!=0 || getDY(ix)!=0 || getDZ(ix)!=0; }
    public boolean isTriggered(int ix) { return data.isSet(3, ix + 0x0D); }
    public boolean ignore3D(int ix) { return data.isSet(1, ix + 7); }
    public boolean justDropped(int ix) { return data.isSet(0, ix + 0x0D); }
    public boolean isEast(int ix) { return data.isSet(0, ix + 0x0D); }
    public boolean isNorth(int ix) { return data.isSet(1, ix + 0x0D); }
    public boolean isWipe(int ix) { return data.isSet(5, ix + 7); }


    public void setGrNo(int ix, int grNo) { data.set(ix, grNo); }
    public void setX(int ix, int x) { data.set(ix + 1, x); }
    public void setY(int ix, int y) { data.set(ix + 2, y); }
    public void setZ(int ix, int z) { data.set(ix + 3, z); }
    public void setW(int ix, int w) { data.set(ix + 4, w); }
    public void setD(int ix, int d) { data.set(ix + 5, d); }
    public void setH(int ix, int h) { data.set(ix + 6, h); }
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
    public void setDeadlyIfPlayerHitsObject(int ix) { data.setBit(5, ix + 0x0D); }
    public void setBothDeadlyFlags(int ix) { data.set(ix + 0x0D, data.getU(ix + 0x0D) | 0xA0);; }
    public void setDead(int ix) { data.setBit(6, ix + 0x0D); }
    public void resetDead(int ix) { data.resetBit(6, ix + 0x0D); }
    public void setDeadOtherHalf(int ix) { setDead(ix + 0x20); }
    public void setIgnore3D(int ix) { data.setBit(1, ix + 7); }
    public void resetIgnore3D(int ix) { data.resetBit(1, ix + 7); }
    public void setTemp10(int ix, int temp10) { data.set(ix + 0x10, temp10); }
    public void setTemp11(int ix, int temp11) { data.set(ix + 0x11, temp11); }
    public void flipMoveable(int ix) { data.set(ix + 7, data.getU(ix + 7) ^ 0x40); }
    public void setRising(int ix) { data.resetBit(2, ix + 0x0D); }
    public void setDropping(int ix) { data.setBit(2, ix + 0x0D); }
    public void setDirUp(int ix) { data.setBit(2, ix + 0x0D); }
    public void setDirDown(int ix) { data.resetBit(2, ix + 0x0D); }
    public void setHFlip(int ix) { data.setBit(6, ix + 0x07); }
    public void resetHFlip(int ix) { data.resetBit(6, ix + 0x07); }
    public void toggleNextPrevSprite(int ix) { setGrNo(ix, getGrNo(ix) ^ 0x01); }
    public void resetTriggered(int ix) { data.resetBit(3, ix + 0x0D); }
    public void clearOob(int ix) { data.set(ix + 0x0C, getFlagsC(ix) & 0xF8); }
    public void resetJustDropped(int ix) { data.resetBit(0, ix + 0x0D); }
    public void toggleEastWest(int ix) { data.set(ix + 0x0D, (data.getU(ix + 0x0D) ^ 0x01)); }
    public void setWipe(int ix) { data.setBit(5, ix+7); }
    public void resetWipe(int ix) { data.resetBit(5, ix+7); }
    public void setPixelX(int ix, int x) { data.set(ix + 0x1A, x); }
    public void setPixelY(int ix, int y) { data.set(ix + 0x1B, y); }
}
