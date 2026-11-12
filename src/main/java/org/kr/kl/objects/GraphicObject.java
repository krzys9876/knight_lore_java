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
}
