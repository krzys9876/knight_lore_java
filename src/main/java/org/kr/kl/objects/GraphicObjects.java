package org.kr.kl.objects;

import org.kr.kl.data.DataBlock;

public class GraphicObjects {
    public final DataBlock data;

    public int start() { return data.start; }
    public int endExcl() { return data.endExcl(); }

    public int[] getCopy() { return data.rawCopy(); }

    public void reset() { data.reset(); }

    public final int[] ghostDeltaTbl = { 0xFF, 1, 0xFE, 2, 0xFD, 3, 0xFC, 4, 0xFB, 5, 0xFA, 6, 0xF9, 7, 0xF8, 8};

    private final GraphicObject[] objects;

    public GraphicObject o(int i) { return objects[i]; }
    public GraphicObject i2o(int ix) { return objects[(ix - start()) / 0x20]; }
    public GraphicObject playerBottom() { return o(0); }
    public GraphicObject playerTop() { return o(1); }
    public final int specialObjStartIx = 2;
    public final int specialObjEndIxExcl = 4;
    public GraphicObject firstSpecObj() { return o(specialObjStartIx); }
    public GraphicObject next(GraphicObject o) {
        int nextIndex = (o.ix - start() + 0x20) / 0x20;
        assert(nextIndex < objects.length);
        return o(nextIndex);
    }
    public GraphicObject prev(GraphicObject o) {
        int nextIndex = (o.ix - start() - 0x20) / 0x20;
        assert(nextIndex >=0);
        return o(nextIndex);
    }
    public final int firstArchIx = 4;
    public final int firstBackgroundObject = 4;
    public GraphicObject firstArch() { return o(firstArchIx); }


    public final int size;

    public GraphicObjects(int start, int size) {
        //data = new DataBlock(0x5C08, 0x40 + 0x40 + 0x20 + 0x0460);
        this.size = size;
        data = new DataBlock(start, size * 0x20);

        objects = new GraphicObject[size];
        for(int i=0; i < this.size; i++) {
            int ix = start() + i * 0x20;
            objects[i] = new GraphicObject(this, ix, data.getU(ix),
                    data.getU(ix + 1), data.getU(ix + 2), data.getU(ix + 3),
                    data.getU(ix + 4), data.getU(ix + 5), data.getU(ix + 6),
                    data.getU(ix + 7), data.getU(ix + 8),
                    data.getU(ix + 9), data.getU(ix + 0x0A), data.getU(ix + 0x0B),
                    data.getU(ix + 0x0C), data.getU(ix + 0x0D),
                    data.getU(ix + 0x0E), data.getU(ix + 0x0F),
                    data.getU(ix + 0x10), data.getU(ix + 0x11),
                    data.getU(ix + 0x12), data.getU(ix + 0x13),
                    data.getU(ix + 0x18), data.getU(ix + 0x19));
        }
    }

    public void resetObject(int ix) {
        for(int i = 0; i < 0x20; i++) data.set(ix + i, 0);
    }

    public String objectInfo(int num, int ix) { return data.objectInfo(num, ix); }

    public int getPixelX(int ix) { return data.getU(ix + 0x1A); }
    public int getPixelY(int ix) { return data.getU(ix + 0x1B); }
    public int getOldSpriteWidthB(int ix) { return data.getU(ix + 0x1C); }
    public int getOldSpriteHeight(int ix) { return data.getU(ix + 0x1D); }
    public int getOldPixelX(int ix) { return data.getU(ix + 0x1E); }
    public int getOldPixelY(int ix) { return data.getU(ix + 0x1F); }

    public void setPixelX(int ix, int x) { data.set(ix + 0x1A, x); }
    public void setPixelY(int ix, int y) { data.set(ix + 0x1B, y); }
    public void setOldSpriteWidthB(int ix, int w) { data.set(ix + 0x1C, w); }
    public void setOldSpriteHeight(int ix, int h) { data.set(ix + 0x1D, h); }
    public void setOldPixelX(int ix, int x) { data.set(ix + 0x1E, x); }
    public void setOldPixelY(int ix, int y) { data.set(ix + 0x1F, y); }
}
