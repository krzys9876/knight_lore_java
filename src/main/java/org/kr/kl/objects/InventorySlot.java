package org.kr.kl.objects;

import org.kr.DataBlock;

public class InventorySlot {
    private final Inventory parent;
    private final int ix;

    public InventorySlot(Inventory parent, int ix) {
        this.parent = parent;
        this.ix = ix;
    }

    public void setGrNo(int grNo) { parent.data.set(ix, grNo); }
    public void setFlags(int f) { parent.data.set(ix + 1, f); }
    public void setPointerL(int l) { parent.data.set(ix + 2, l); }
    public void setPointerH(int h) { parent.data.set(ix + 3, h); }

    public int getGrNo() { return parent.data.getU(ix); }
    public int getFlags() { return parent.data.getU(ix + 1); }
    public int getPointerL() { return parent.data.getU(ix + 2); }
    public int getPointerH() { return parent.data.getU(ix + 3); }

    public void copyFrom(InventorySlot other) {
        this.setGrNo(other.getGrNo());
        this.setFlags(other.getFlags());
        this.setPointerL(other.getPointerL());
        this.setPointerH(other.getPointerH());
    }

    public void reset() {
        this.setGrNo(0);
        this.setFlags(0);
        this.setPointerL(0);
        this.setPointerH(0);
    }
}
