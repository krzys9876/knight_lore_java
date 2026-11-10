package org.kr.kl.objects;

/*; #TABLE(default,centre,:w)
        ; { =h Byte(n) | =h Description }
        ; { +0     | graphic_no. }
        ; { +1     | x (center) }
        ; { +2     | y (center) }
        ; { +3     | z (bottom) }
        ; { +4     | width (X radius) }
        ; { +5     | depth (Y radius) }
        ; { +6     | height }
        ; { +7     | flags }
        ; {        | - 7=vflip sprite }
        ; {        | - 6=hflip sprite }
        ; {        | - 5=wipe }
        ; {        | - 4=draw }
        ; {        | - 3=auto-adjust near arches (player only) }
        ; {        | - 2=moveable }
        ; {        | - 1=ignore in 3D calculations }
        ; {        | - 0=is near arch (player only) }
        ; { +8     | screen }
        ; { +9     | dX }
        ; { +10    | dY }
        ; { +11    | dZ }
        ; { +12    | counter and flags }
        ; {        | - 7-4=counter when entering screen }
        ; {        | - 3=jumping }
        ; {        | - 2=Z out-of-bounds }
        ; {        | - 1=Y out-of-bounds }
        ; {        | - 0=X out-of-bounds }
        ; { +13    | per-object info/flags }
        ; {        | - direction and counters for looking, turning }
        ; {        | - 7=deadly if object hits player }
        ; {        | - 6=dead }
        ; {        | - 5=deadly if player hits object }
        ; {        | - 4=(not used) }
        ; {        | - 3=triggered (dropping, collapsing blocks) }
        ; {        | - 2=up (bouncing ball), dropping (spiked ball) }
        ; {        | - 1=north (NS fire) }
        ; {        | - 0=east (WE fire, EW guard), just dropped (spec objs) }
        ; { +14    | d_x_adj }
        ; { +15    | d_y_adj }
        ; { +16-17 | ptr object table entry or tmp player graphic_no }
        ; { +18    | pixel X adjustment }
        ; { +19    | pixel Y adjustment }
        ; { +20-23 | unused }
        ; { +24    | sprite data width (bytes) }
        ; { +25    | sprite data height (lines) }
        ; { +26    | pixel X }
        ; { +27    | pixel Y }
        ; { +28    | old sprite data width (bytes) }
        ; { +29    | old sprite data height (lines) }
        ; { +30    | old pixel X }
        ; { +31    | old pixel Y }
*/

import org.kr.DataBlock;

public class GraphicObjects {
    public final DataBlock graphic_objs_tbl_5C08;

    public int start() {
        return graphic_objs_tbl_5C08.start;
    }

    public int[] getCopy() {
        return graphic_objs_tbl_5C08.getCopy();
    }

    public final int size;

    public GraphicObjects() {
        graphic_objs_tbl_5C08 = new DataBlock(0x5C08, 0x40 + 0x40 + 0x20 + 0x0460);
        size = graphic_objs_tbl_5C08.size / 32;
    }

    public int getGrNo(int ix) { return graphic_objs_tbl_5C08.getU(ix); }
    public int getX(int ix) { return graphic_objs_tbl_5C08.getU(ix + 1); }
    public int getY(int ix) { return graphic_objs_tbl_5C08.getU(ix + 2); }
    public int getZ(int ix) { return graphic_objs_tbl_5C08.getU(ix + 3); }
    public int getW(int ix) { return graphic_objs_tbl_5C08.getU(ix + 4); }
    public int getD(int ix) { return graphic_objs_tbl_5C08.getU(ix + 5); }
    public int getH(int ix) { return graphic_objs_tbl_5C08.getU(ix + 6); }
    public int getDX(int ix) { return graphic_objs_tbl_5C08.getS(ix + 0x09); }
    public int getDXu(int ix) { return graphic_objs_tbl_5C08.getU(ix + 0x09); }
    public int getDY(int ix) { return graphic_objs_tbl_5C08.getS(ix + 0x0A); }
    public int getDYu(int ix) { return graphic_objs_tbl_5C08.getU(ix + 0x0A); }
    public int getDZ(int ix) { return graphic_objs_tbl_5C08.getS(ix + 0x0B); }
    public int getFlags7(int ix) { return graphic_objs_tbl_5C08.getS(ix + 7); }
    public int getFlagsC(int ix) { return graphic_objs_tbl_5C08.getS(ix + 0x0C); }
    public int getTemp10(int ix) { return graphic_objs_tbl_5C08.getU(ix + 0x10); }
    public int getTemp11(int ix) { return graphic_objs_tbl_5C08.getU(ix + 0x11); }
    public int getYAdj(int ix) { return graphic_objs_tbl_5C08.getS(ix + 0x13); }
    public boolean isHFlip(int ix) { return graphic_objs_tbl_5C08.isSet(6, ix + 0x07); } // BIT 6,(IX+$07)
    public boolean isAutoAdjust(int ix) { return graphic_objs_tbl_5C08.isSet(3, ix + 0x07); } // BIT 6,(IX+$07)
    public boolean isOobX(int ix) { return graphic_objs_tbl_5C08.isSet(0, ix + 0x0C); }
    public boolean isOobY(int ix) { return graphic_objs_tbl_5C08.isSet(1, ix + 0x0C); }
    public boolean isOobXY(int ix) { return isOobX(ix) || isOobY(ix); }
    public boolean isOobZ(int ix) { return graphic_objs_tbl_5C08.isSet(2, ix + 0x0C); }
    public boolean isDead(int ix) { return graphic_objs_tbl_5C08.isSet(6, ix + 0x0D); }
    public boolean isJumping(int ix) { return graphic_objs_tbl_5C08.isSet(3, ix + 0x0C); }
    public boolean isDropping(int ix) { return graphic_objs_tbl_5C08.isSet(2, ix + 0x0D); }
    public boolean isMoving(int ix) { return getDX(ix)!=0 || getDY(ix)!=0 || getDZ(ix)!=0; }
    public boolean isTriggered(int ix) { return graphic_objs_tbl_5C08.isSet(3, ix + 0x0D); }
    public boolean ignore3D(int ix) { return graphic_objs_tbl_5C08.isSet(1, ix + 7); }
    public boolean justDropped(int ix) { return graphic_objs_tbl_5C08.isSet(0, ix + 0x0D); }

    public void setGrNo(int ix, int grNo) { graphic_objs_tbl_5C08.set(ix, grNo); }
    public void setX(int ix, int x) { graphic_objs_tbl_5C08.set(ix + 1, x); }
    public void setY(int ix, int y) { graphic_objs_tbl_5C08.set(ix + 2, y); }
    public void setZ(int ix, int z) { graphic_objs_tbl_5C08.set(ix + 3, z); }
    public void setW(int ix, int w) { graphic_objs_tbl_5C08.set(ix + 4, w); }
    public void setD(int ix, int d) { graphic_objs_tbl_5C08.set(ix + 5, d); }
    public void setH(int ix, int h) { graphic_objs_tbl_5C08.set(ix + 6, h); }
    public void setDX(int ix, int dx) { graphic_objs_tbl_5C08.set(ix + 0x09, dx); }
    public void setDY(int ix, int dy) { graphic_objs_tbl_5C08.set(ix + 0x0A, dy); }
    public void setDZ(int ix, int dz) { graphic_objs_tbl_5C08.set(ix + 0x0B, dz); }
    public void setDXAdj(int ix, int dxAdj) { graphic_objs_tbl_5C08.set(ix + 0x0E, dxAdj); }
    public void setDYAdj(int ix, int dyAdj) { graphic_objs_tbl_5C08.set(ix + 0x0F, dyAdj); }
    public void setXAdj(int ix, int xAdj) { graphic_objs_tbl_5C08.set(ix + 0x12, xAdj); }
    public void setYAdj(int ix, int yAdj) { graphic_objs_tbl_5C08.set(ix + 0x13, yAdj); }
    public void setXYAdj(int ix, int xAdj, int yAdj) {
        setXAdj(ix, xAdj);
        setYAdj(ix, yAdj);
    }
    public void setDeadlyIfObjectHitsPlayer(int ix) { graphic_objs_tbl_5C08.setBit(7, ix + 0x0D); }
    public void setDeadlyIfPlayerHitsObject(int ix) { graphic_objs_tbl_5C08.setBit(5, ix + 0x0D); }
    public void setBothDeadlyFlags(int ix) { graphic_objs_tbl_5C08.set(ix + 0x0D, graphic_objs_tbl_5C08.getU(ix + 0x0D) | 0xA0);; }
    public void setDead(int ix) { graphic_objs_tbl_5C08.setBit(6, ix + 0x0D); }
    public void setDeadOtherHalf(int ix) { setDead(ix + 0x20); }
    public void setIgnore3D(int ix) { graphic_objs_tbl_5C08.setBit(1, ix + 7); }
    public void resetIgnore3D(int ix) { graphic_objs_tbl_5C08.resetBit(1, ix + 7); }
    public void setTemp10(int ix, int temp10) { graphic_objs_tbl_5C08.set(ix + 0x10, temp10); }
    public void setTemp11(int ix, int temp11) { graphic_objs_tbl_5C08.set(ix + 0x11, temp11); }
    public void flipMoveable(int ix) { graphic_objs_tbl_5C08.set(ix + 7, graphic_objs_tbl_5C08.getU(ix + 7) ^ 0x40); }
    public void setRising(int ix) { graphic_objs_tbl_5C08.resetBit(2, ix + 0x0D); }
    public void setDropping(int ix) { graphic_objs_tbl_5C08.setBit(2, ix + 0x0D); }
    public void setHFlip(int ix) { graphic_objs_tbl_5C08.setBit(6, ix + 0x07); }
    public void resetHFlip(int ix) { graphic_objs_tbl_5C08.resetBit(6, ix + 0x07); }
    public void toggleNextPrevSprite(int ix) { setGrNo(ix, getGrNo(ix) ^ 0x01); }
    public void resetTriggered(int ix) { graphic_objs_tbl_5C08.resetBit(3, ix + 0x0D); }
    public void clearOob(int ix) { graphic_objs_tbl_5C08.set(ix + 0x0C, getFlagsC(ix) & 0xF8); }
    public void resetJustDropped(int ix) { graphic_objs_tbl_5C08.resetBit(0, ix + 0x0D); }
}
