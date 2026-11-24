package org.kr.kl.objects;

import org.kr.kl.data.DataBlock;
import org.kr.kl.data.InitialData;

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

public class GraphicObject {
    private final GraphicObjects parent;
    public final int ix;

    private int grNo;
    private int x, y, z;
    private int w, d, h;
    private int flags7;
    private int screen;
    private int dx, dy, dz;
    private int flagsC;

    public GraphicObject(GraphicObjects parent, int ix, int grNo, int x, int y, int z, int w, int d, int h,
                         int flags7, int screen, int dx, int dy, int dz, int flagsC) {
        this.parent = parent;
        this.ix = ix;
        this.grNo = grNo;
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        this.d = d;
        this.h = h;
        this.flags7 = flags7;
        this.screen = screen;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.flagsC = flagsC;
    }

    public GraphicObject(GraphicObjects parent, int ix) {
        this(parent, ix, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    public GraphicObject() {
        this(new GraphicObjects(0, 1), 0);
    }

    public GraphicObject next() { return parent.next(this);}
    public GraphicObject prev() { return parent.prev(this);}

    public void reset() {
        setGrNo(0);
        setX(0);
        setY(0);
        setZ(0);
        setW(0);
        setD(0);
        setH(0);
        setFlags7(0);
        setScreen(0);
        setDX(0);
        setDY(0);
        setDZ(0);
        setFlagsC(0);
        setFlagsD(0);
        setDXAdj(0);
        setDYAdj(0);
        setTemp10(0);
        setTemp11(0);
        setXAdj(0);
        setYAdj(0);
        setSpriteWidthB(0);
        setSpriteHeight(0);
        setPixelX(0);
        setPixelY(0);
        setOldSpriteWidthB(0);
        setOldSpriteHeight(0);
        setOldPixelX(0);
        setOldPixelY(0);

        parent.resetObject(ix);
    }

    private static final DataBlock cauldron_bubbles_B8C8 =  InitialData.block("cauldron_bubbles_B8C8");

    public void makeCauldronBubbles() {
        copyFromInit(cauldron_bubbles_B8C8, 0);
        setScreen(cauldron_bubbles_B8C8.getU(cauldron_bubbles_B8C8.start + 8));
        setDX(cauldron_bubbles_B8C8.getU(cauldron_bubbles_B8C8.start + 9));
        setDY(cauldron_bubbles_B8C8.getU(cauldron_bubbles_B8C8.start + 0x0A));
        setDZ(cauldron_bubbles_B8C8.getU(cauldron_bubbles_B8C8.start + 0x0B));
        setFlagsC(cauldron_bubbles_B8C8.getU(cauldron_bubbles_B8C8.start + 0x0C));
        setFlagsD(cauldron_bubbles_B8C8.getU(cauldron_bubbles_B8C8.start + 0x0D));
        setDXAdj(cauldron_bubbles_B8C8.getU(cauldron_bubbles_B8C8.start + 0x0E));
        setDYAdj(cauldron_bubbles_B8C8.getU(cauldron_bubbles_B8C8.start + 0x0F));
        setTemp10(cauldron_bubbles_B8C8.getU(cauldron_bubbles_B8C8.start + 0x10));
        setTemp11(cauldron_bubbles_B8C8.getU(cauldron_bubbles_B8C8.start + 0x11));
    }

    public void copyFromInit(DataBlock other, int offset) {
        offset += other.start;
        setGrNo(other.getU(offset));
        setX(other.getU(offset + 1));
        setY(other.getU(offset + 2));
        setZ(other.getU(offset + 3));
        setW(other.getU(offset + 4));
        setD(other.getU(offset + 5));
        setH(other.getU(offset + 6));
        setFlags7(other.getU(offset + 7));
    }

    public void copyCoordsFrom(GraphicObject other) {
        setX(other.getX());
        setY(other.getY());
        setZ(other.getZ());
        setW(other.getW());
        setD(other.getD());
        setH(other.getH());
        setFlags7(other.getFlags7());
    }

    public void copyFrom(GraphicObject other) {
        setGrNo(other.getGrNo());
        copyCoordsFrom(other);
        setScreen(other.getScreen());
        setDX(other.getDX());
        setDY(other.getDY());
        setDZ(other.getDZ());
        setFlagsC(other.getFlagsC());
        setFlagsD(other.getFlagsD());
        setDXAdj(other.getDXAdj());
        setDYAdj(other.getDYAdj());
        setTemp10(other.getTemp10());
        setTemp11(other.getTemp11());
        setXAdj(other.getXAdj());
        setYAdj(other.getYAdj());
        setSpriteWidthB(other.getSpriteWidthB());
        setSpriteHeight(other.getSpriteHeight());
        setPixelX(other.getPixelX());
        setPixelY(other.getPixelY());
        setOldSpriteWidthB(other.getOldSpriteWidthB());
        setOldSpriteHeight(other.getOldSpriteHeight());
        setOldPixelX(other.getOldPixelX());
        setOldPixelY(other.getOldPixelY());

    }

    public void initFrom(BackgroundSprite sprite, int screenNo) {
        reset();
        setGrNo(sprite.grNo());
        setX(sprite.x());
        setY(sprite.y());
        setZ(sprite.z());
        setW(sprite.w());
        setD(sprite.d());
        setH(sprite.h());
        setFlags7(sprite.flags());
        setScreen(screenNo);
    }

    public void initFrom(ForegroundSprite sprite, int screenNo, int x, int y, int z, int roomSize) {
        reset();
        setGrNo(sprite.grNo());
        setW(sprite.w());
        setD(sprite.d());
        setH(sprite.h());
        setFlags7(sprite.flags());
        setScreen(screenNo);
        int offsets = sprite.offsets();
        int x1 = ((offsets << 3) & 8);
        int y1 = ((offsets << 2) & 8);
        setX(x1 + x * 16 + 0x48); // X
        setY(y1 + y * 16 + 0x48); // Y
        setZ(((z * 12 + offsets) & 0xFC) + roomSize); // Y, variable stores room size Z
    }


    public int getGrNo() { return this.grNo; }
    public void setGrNo(int grNo) { this.grNo = grNo; }
    public void setDeathSparkles() { setGrNo(0x70); }
    public void setBitGrNo(int bit) { setGrNo(DataBlock.getWithBitSetS(bit, getGrNo())); }
    public void resetBitGrNo(int bit) { setGrNo(DataBlock.getWithBitResetS(bit, getGrNo())); }
    public void toggleBitGrNo(int bit) { setGrNo(DataBlock.getWithBitToggledS(bit, getGrNo())); }
    public boolean isBitSetGrNo(int bit) { return DataBlock.isSetS(bit, getGrNo()); }
    public boolean isSparkles() { return ((getGrNo() - 0x10) & 0xFF) >= 0x40; }
    public boolean isSpecialObject() { return ((getGrNo() - 0x60) & 0xFF) < 0x07; }
    public boolean isWulf() { return ((getGrNo() - 0x10) & 0xFF) >= 0x20; }
    public int getSpriteDir() {
        // Derrive direction from player / guard sprite
        int flags = isHFlip() ? 0x10 : 0;
        int grNo = (getGrNo() & 8) | flags;
        grNo = (grNo >> 3) & 3;
        return grNo;
    }
    public void toggleNextPrevSprite() { setGrNo(getGrNo() ^ 0x01); }

    public void nextGrNoMod4() {
        int grNo = getGrNo();
        int grNo2Bits = grNo & 0x03;
        int newGrNo = grNo & 0xFC | ((grNo2Bits + 1) & 0x03); // ; inc low 2 bits
        setGrNo(newGrNo);
    }


    //; abs(objX+newdX-thisX)-(objW+thisW), o1 - obj, o2 - this
    public boolean intersectOnX(GraphicObject other, int dxAdj) {
        return Math.abs(getX() + dxAdj - other.getX()) - (getW() + other.getW()) < 0;
    }

    //; (objY+l-thisY)-(objD+thisD) - note: there is an error in comment in original source
    public boolean intersectOnY(GraphicObject other, int dyAdj) {
        return Math.abs(getY() + dyAdj - other.getY()) - (getD() + other.getD()) < 0;
    }

    //; (objZ+H-thisZ)-(objH or thisH)
    public boolean intersectOnZ(GraphicObject other, int dzAdj) {
        int check = getZ() + dzAdj - other.getZ();
        int d = check >= 0 ? other.getH() : getH();
        return Math.abs(check) - d < 0;
    }

    public boolean isNotIgnored() { return getGrNo() != 0 && !ignore3D(); }

    public int getX() { return DataBlock.getUS(x); }
    public int getY() { return DataBlock.getUS(y); }
    public int getZ() { return DataBlock.getUS(z); }
    public int getW() { return DataBlock.getUS(w); }
    public int getD() { return DataBlock.getUS(d); }
    public int getH() { return DataBlock.getUS(h); }
    public int getDX() { return DataBlock.getSS(dx); }
    public int getDXu() { return DataBlock.getUS(dx); }
    public int getDY() { return DataBlock.getSS(dy); }
    public int getDYu() { return DataBlock.getUS(dy); }
    public int getDZ() { return DataBlock.getSS(dz); }
    public int getDZu() { return DataBlock.getUS(dz); }
    public int getXAdj() { return parent.getXAdj(ix); }
    public int getYAdj() { return parent.getYAdj(ix); }
    public int getDXAdj() { return parent.getDXAdj(ix); }
    public int getDYAdj() { return parent.getDYAdj(ix); }
    public boolean isMoving() { return getDX()!=0 || getDY()!=0 || getDZ()!=0; }
    public boolean isMovingXY() { return getDX()!=0 || getDY()!=0; }

    public int getPixelX() { return parent.getPixelX(ix); }
    public int getPixelY() { return parent.getPixelY(ix); }
    public void setPixelX(int x) { parent.setPixelX(ix, x); }
    public void setPixelY(int y) { parent.setPixelY(ix, y); }

    public int getTemp10() { return parent.getTemp10(ix); }
    public int getTemp11() { return parent.getTemp11(ix); }

    public void setTemp10(int temp10) { parent.setTemp10(ix, temp10); }
    public void setTemp11(int temp11) { parent.setTemp11(ix, temp11); }

    public int getScreen() { return screen; }
    public void setScreen(int screen) { this.screen = screen; }
    public boolean isCauldronRoom() { return getScreen() == 0x88; }
    public boolean isOddRoom() { return (getScreen() & 1) == 1; }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setZ(int z) { this.z = z; }
    public void setW(int w) { this.w = w; }
    public void setD(int d) { this.d = d; }
    public void setH(int h) { this.h = h; }
    public void setDX(int dx) { this.dx = dx; }
    public void setDY(int dy) { this.dy = dy; }
    public void setDZ(int dz) { this.dz = dz; }
    public void setDXAdj(int dxAdj) { parent.setDXAdj(ix, dxAdj); }
    public void setDYAdj(int dyAdj) { parent.setDYAdj(ix, dyAdj); }
    public void setXAdj(int xAdj) { parent.setXAdj(ix, xAdj); }
    public void setYAdj(int yAdj) { parent.setYAdj(ix, yAdj); }
    public void setXYAdj(int xAdj, int yAdj) { parent.setXYAdj(ix, xAdj, yAdj); }
    public void setSpriteWidthB(int w) { parent.setSpriteWidthB(ix, w); }
    public void setSpriteHeight(int h) { parent.setSpriteHeight(ix, h); }

    public boolean isDead() { return parent.isDead(ix); }
    public void setDead() { parent.setDead(ix); }
    public void resetDead() { parent.resetDead(ix); }
    public void setDeadlyIfObjectHitsPlayer() { parent.setDeadlyIfObjectHitsPlayer(ix); }
    public void setBothDeadlyFlags() { parent.setBothDeadlyFlags(ix); }
    public boolean getDeadlyIfObjectHitsPlayer() { return parent.getDeadlyIfObjectHitsPlayer(ix); }
    public boolean getDeadlyIfPlayerHitsObject() { return parent.getDeadlyIfPlayerHitsObject(ix); }


    public int getSpriteWidthB() { return parent.getSpriteWidthB(ix); }
    public int getSpriteHeight() { return parent.getSpriteHeight(ix); }
    public int getOldSpriteWidthB() { return parent.getOldSpriteWidthB(ix); }
    public int getOldSpriteHeight() { return parent.getOldSpriteHeight(ix); }
    public void setOldSpriteWidthB(int w) { parent.setOldSpriteWidthB(ix, w); }
    public void setOldSpriteHeight(int h) { parent.setOldSpriteHeight(ix, h); }
    public int getOldPixelX() { return parent.getOldPixelX(ix); }
    public int getOldPixelY() { return parent.getOldPixelY(ix); }
    public void setOldPixelX(int x) { parent.setOldPixelX(ix, x); }
    public void setOldPixelY(int y) { parent.setOldPixelY(ix, y); }

    public void save2DInfo() {
        setOldSpriteWidthB(getSpriteWidthB());
        setOldSpriteHeight(getSpriteHeight());
        setOldPixelX(getPixelX());
        setOldPixelY(getPixelY());
    }



    // Flag 7
    public int getFlags7() { return flags7; }
    public void setFlags7(int f) { this.flags7 = f; }

    public boolean isNearArch() { return Flags7.isNearArch(getFlags7()); }
    public void setNearArch() { setFlags7(Flags7.setNearArch(getFlags7())); }
    public void resetNearArch() { setFlags7(Flags7.resetNearArch(getFlags7())); }

    public boolean ignore3D() { return Flags7.ignore3D(getFlags7()); }
    public void setIgnore3D() { setFlags7(Flags7.setIgnore3D(getFlags7())); }
    public void resetIgnore3D() { setFlags7(Flags7.resetIgnore3D(getFlags7())); }

    public boolean isMoveable() { return Flags7.isMoveable(getFlags7()); }

    public boolean isAutoAdjust() { return Flags7.isAutoAdjust(getFlags7()); }

    public void setDraw() { setFlags7(Flags7.setDraw(getFlags7())); }

    public void setWipe() { setFlags7(Flags7.setWipe(getFlags7())); }
    public void resetWipe() { setFlags7(Flags7.resetWipe(getFlags7())); }
    public void setWipeAndDraw() {
        // Not used - al objects are redrawn every tick
        setWipe();
        setDraw();
    }

    public boolean isHFlip() { return Flags7.isHFlip(getFlags7()); }
    public void setHFlip() { setFlags7(Flags7.setHFlip(getFlags7())); }
    public void resetHFlip() { setFlags7(Flags7.resetHFlip(getFlags7())); }
    public void toggleHFlip() { setFlags7(Flags7.toggleHFlip(getFlags7())); }

    public boolean isVFlip() { return Flags7.isVFlip(getFlags7()); }

    // Flags C
    public int getFlagsC() { return flagsC; }
    public void setFlagsC(int f) { this.flagsC = f; }

    public int getEnterCounter() { return getFlagsC() & 0xF0; }
    public boolean enteringScreen() { return getEnterCounter() > 0; }
    public void initEnterCounter() { setFlagsC(getFlagsC() | 0x30); }
    public void decEnterCounter() {
        int counter = getEnterCounter();
        if(counter >= 0x10) setFlagsC((getFlagsC() & 0x0F) + counter - 0x10);
    }

    public boolean isOobX() { return DataBlock.isSetS(0, getFlagsC()); }
    public boolean isOobY() { return DataBlock.isSetS(1, getFlagsC()); }
    public boolean isOobXY() { return isOobX() || isOobY(); }
    public boolean isOobZ() { return DataBlock.isSetS(2, getFlagsC()); }

    public void setOobX() { setFlagsC(DataBlock.getWithBitSetS(0, getFlagsC())); }
    public void setOobY() { setFlagsC(DataBlock.getWithBitSetS(1, getFlagsC())); }
    public void setOobZ() { setFlagsC(DataBlock.getWithBitSetS(2, getFlagsC())); }
    public void clearOob() { setFlagsC(getFlagsC() & 0xF8); }

    public boolean isJumping() { return parent.isJumping(ix); }
    public void setJumping() { parent.setJumping(ix); }
    public void resetJumping() { parent.resetJumping(ix); }

    // Flag D
    public int getFlagsD() { return parent.getFlagsD(ix); }
    public void setFlagsD(int f) { parent.setFlagsD(ix, f); }

    public boolean isDropping() { return parent.isDropping(ix); }
    public void setRising() { parent.setRising(ix); }
    public void setDropping() { parent.setDropping(ix); }
    public boolean isDirUp() { return parent.isDirUp(ix); }
    public void setDirUp() { parent.setDirUp(ix); }
    public void setDirDown() { parent.setDirDown(ix); }

    public boolean isTriggered() { return parent.isTriggered(ix); }
    public boolean justDropped() { return parent.justDropped(ix); }
    public void setJustDropped() { parent.setJustDropped(ix); }
    public void resetJustDropped() { parent.resetJustDropped(ix); }

    public void setTriggered() { parent.setTriggered(ix); }
    public void resetTriggered() { parent.resetTriggered(ix); }

    public boolean isEast() { return parent.isEast(ix); }
    public boolean isNorth() { return parent.isNorth(ix); }
    public int getDirection() { return parent.getDirection(ix); }
    public void incDirection() { parent.incDirection(ix); }
    public void toggleEastWest() { parent.toggleEastWest(ix); }
    public void toggleNorthSouth() { parent.toggleNorthSouth(ix); }

    public int getLookAroundCounter() { return parent.getLookAroundCounter(ix); } // NOTE: multi-purpose bits
    public void decLookAroundCounter() { parent.decLookAroundCounter(ix); }
    public void initLookAroundCounter() { parent.initLookAroundCounter(ix); }

    public int getTurnCounter() { return parent.getTurnCounter(ix); }
    public void initTurnCounter() { parent.initTurnCounter(ix); }
    public void decTurnCounter() { parent.decTurnCounter(ix); }

}
