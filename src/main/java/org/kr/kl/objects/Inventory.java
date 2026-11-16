package org.kr.kl.objects;

import org.kr.DataBlock;

public class Inventory {
    public final DataBlock data = new DataBlock(0x5BD8, 16);

    public InventorySlot[] slots;

    public Inventory() {
        slots = new InventorySlot[4];
        for(int i=0; i<4; i++) slots[i] = new InventorySlot(this, data.start + i*4);
    }

    public void reset() { data.reset(); }

    public InventorySlot currentSlot() { return slots[3]; }
    public InventorySlot firstSlot() { return slots[0]; }

    public void adjustCarried() {
        for(int i=3; i>0; i--) slots[i].copyFrom(slots[i-1]);
        slots[0].reset();
    }
}
