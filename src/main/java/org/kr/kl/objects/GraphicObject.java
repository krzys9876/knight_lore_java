package org.kr.kl.objects;

public class GraphicObject {
    private final GraphicObjects parent;
    public final int ix;

    public GraphicObject(GraphicObjects parent, int ix) {
        this.parent = parent;
        this.ix = ix;
    }

    public GraphicObjects getPaarent() { return parent; }
    public GraphicObject next() { return parent.next(this);}

    public int getGrNo() { return parent.getGrNo(ix); }
    public void setGrNo(int grNo) { parent.setGrNo(ix, grNo); }
    public void setBitGrNo(int bit) { parent.setBitGrNo(ix, bit); }
    public void resetBitGrNo(int bit) { parent.resetBitGrNo(ix, bit); }
    public void toggleBitGrNo(int bit) { parent.toggleBitGrNo(ix, bit); }
    public boolean isBitSetGrNo(int bit) { return parent.isBitSetGrNo(ix, bit); }
    public boolean isSparkles() { return ((getGrNo() - 0x10) & 0xFF) >= 0x40; }

    public int getX() { return parent.getX(ix); }
    public int getY() { return parent.getY(ix); }
    public int getZ() { return parent.getZ(ix); }
    public int getW() { return parent.getW(ix); }
    public int getD() { return parent.getD(ix); }
    public int getH() { return parent.getH(ix); }
    public int getDX() { return parent.getDX(ix); }
    public int getDXu() { return parent.getDXu(ix); }
    public int getDY() { return parent.getDY(ix); }
    public int getDYu() { return parent.getDYu(ix); }
    public int getDZ() { return parent.getDZ(ix); }
    public int getDZu() { return parent.getDZu(ix); }
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

    public int getScreen() { return parent.getScreen(ix); }
    public void setScreen(int screen) { parent.setScreen(ix, screen); }

    public void setX(int x) { parent.setX(ix, x); }
    public void setY(int y) { parent.setY(ix, y); }
    public void setZ(int z) { parent.setZ(ix, z); }
    public void setW(int w) { parent.setW(ix, w); }
    public void setD(int d) { parent.setD(ix, d); }
    public void setH(int h) { parent.setH(ix, h); }
    public void setDX(int dx) { parent.setDX(ix, dx); }
    public void setDY(int dy) { parent.setDY(ix, dy); }
    public void setDZ(int dz) { parent.setDZ(ix, dz); }
    public void setDXAdj(int dxAdj) { parent.setDXAdj(ix, dxAdj); }
    public void setDYAdj(int dyAdj) { parent.setDY(ix, dyAdj); }
    public void setXAdj(int xAdj) { parent.setXAdj(ix, xAdj); }
    public void setYAdj(int yAdj) { parent.setYAdj(ix, yAdj); }
    public void setXYAdj(int xAdj, int yAdj) { parent.setXYAdj(ix, xAdj, yAdj); }

    public boolean isDead() { return parent.isDead(ix); }
    public void setDead() { parent.setDead(ix); }
    public void resetDead() { parent.resetDead(ix); }
    public void setDeadlyIfObjectHitsPlayer() { parent.setDeadlyIfObjectHitsPlayer(ix); }
    //public void setDeadlyIfPlayerHitsObject(int ix) { data.setBit(5, ix + 0x0D); }
    public void setBothDeadlyFlags() { parent.setBothDeadlyFlags(ix); }


    public int getSpriteWidthB() { return parent.getSpriteWidthB(ix); }
    public int getSpriteHeight() { return parent.getSpriteHeight(ix); }
    public void setOldSpriteWidthB(int w) { parent.setOldSpriteWidthB(ix, w); }
    public void setOldSpriteHeight(int h) { parent.setOldSpriteHeight(ix, h); }
    public void setOldPixelX(int x) { parent.setOldPixelX(ix, x); }
    public void setOldPixelY(int y) { parent.setOldPixelY(ix, y); }

    public void save2DInfo() {
        setOldSpriteWidthB(getSpriteWidthB());
        setOldSpriteHeight(getSpriteHeight());
        setOldPixelX(getPixelX());
        setOldPixelY(getPixelY());
    }



    // Flag 7
    public int getFlags7() { return parent.getFlags7(ix); }
    //public void setFlags7(int ix, int f) { data.set(ix + 7, f); }

    public boolean isNearArch() { return parent.isNearArch(ix); }
    public void setNearArch() { parent.setNearArch(ix); }
    public void resetNearArch() { parent.resetNearArch(ix); }

    public boolean ignore3D() { return parent.ignore3D(ix); }
    public void setIgnore3D() { parent.setIgnore3D(ix); }
    public void resetIgnore3D() { parent.resetIgnore3D(ix); }

    //public boolean isMoveable(int ix) { return Flags7.isMoveable(getFlags7(ix)); }

    public boolean isAutoAdjust() { return parent.isAutoAdjust(ix); }

    //public void setDraw(int ix) { setFlags7(ix, Flags7.setDraw(getFlags7(ix))); }

    //public void setWipe(int ix) { setFlags7(ix, Flags7.setWipe(getFlags7(ix))); }
    //public void resetWipe(int ix) { setFlags7(ix, Flags7.resetWipe(getFlags7(ix))); }

    public boolean isHFlip() { return parent.isHFlip(ix); }
    public void setHFlip() { parent.setHFlip(ix); }
    public void resetHFlip() { parent.resetHFlip(ix); }
    public void toggleHFlip() { parent.toggleHFlip(ix); }

    public boolean isVFlip() { return parent.isVFlip(ix); }

    // Flags C
    //public int getFlagsC(int ix) { return data.getS(ix + 0x0C); }
    //public void setFlagsC(int ix, int f) { data.set(ix + 0x0C, f); }

    //public int getEnterCounter(int ix) { return getFlagsC(ix) & 0xF0; }
    public boolean enteringScreen() { return parent.enteringScreen(ix); }
    //public void initEnterCounter(int ix) { setFlagsC(ix, getFlagsC(ix) | 0x30); }
    //public void decEnterCounter(int ix) {
    //    int counter = getEnterCounter(ix);
    //    if(counter >= 0x10) setFlagsC(ix, (getFlagsC(ix) & 0x0F) + counter - 0x10);
    //}

    public boolean isOobX() { return parent.isOobX(ix); }
    public boolean isOobY() { return parent.isOobY(ix); }
    public boolean isOobXY() { return isOobX() || isOobY(); }
    public boolean isOobZ() { return parent.isOobZ(ix); }

    //public void setOobX(int ix) { data.setBit(0, ix + 0x0C); }
    //public void setOobY(int ix) { data.setBit(1, ix + 0x0C); }
    //public void setOobZ(int ix) { data.setBit(2, ix + 0x0C); }

    // Flag D

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

}
