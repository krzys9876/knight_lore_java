package org.kr.kl.objects;

public class GraphicObject {
    private final GraphicObjects parent;
    public final int ix;

    public GraphicObject(GraphicObjects parent, int ix) {
        this.parent = parent;
        this.ix = ix;
    }

    public GraphicObjects getPaarent() { return parent; }

    public int getGrNo() { return parent.getGrNo(ix); }
    public void setGrNo(int grNo) { parent.setGrNo(ix, grNo); }

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
    public int getDXAdj() { return parent.getDXAdj(ix); }
    public int getDYAdj() { return parent.getDYAdj(ix); }


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

    //public boolean isNearArch(int ix) { return Flags7.isNearArch(getFlags7(ix)); }
    //public void setNearArch(int ix) { setFlags7(ix, Flags7.setNearArch(getFlags7(ix))); }
    //public void resetNearArch(int ix) { setFlags7(ix, Flags7.resetNearArch(getFlags7(ix))); }

    //public boolean ignore3D(int ix) { return Flags7.ignore3D(getFlags7(ix)); }
    //public void setIgnore3D(int ix) { setFlags7(ix, Flags7.setIgnore3D(getFlags7(ix))); }
    //public void resetIgnore3D(int ix) { setFlags7(ix, Flags7.resetIgnore3D(getFlags7(ix))); }

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

}
