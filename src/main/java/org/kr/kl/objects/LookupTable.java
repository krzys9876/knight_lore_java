package org.kr.kl.objects;

import org.kr.DataBlock;

public class LookupTable {
    private final DataBlock data = new DataBlock(0xF100, 0xFFFF - 0xF100 + 1);

    private final DataBlock reverse = new DataBlock(0, 256);

    public LookupTable() {
        build();
    }

    public int get(int address) { return data.getU(address); }

    public int getReverse(int v) { return reverse.getU(v); }

    private void build() {
        // F200 - FFFF
        for(int offset = 1; offset < 8; offset ++) {
            int base = 0xF000 + (offset * 0x0200);
            for(int i=0; i<256; i++) {
                data.set(base + i, shiftLeftPart(i, offset)); // F200, F400...
                data.set(base + i + 0x100, shiftRightPart(i, offset)); // F300. F500...
            }
        }
        // original addresses: F100 - F1FF
        for(int l = 0; l<=0xFF; l++) reverse.set(l, reverseBits(l));
        // Lookup table values verified with KL memory dump
    }

    private int reverseBits(int v) {
        int e = 0;
        for(int b=0; b<8; b++) {
            int bit = (v & (1 << b)) >> b; // given bit shifted to position 0
            e |= bit << (7-b); // shift bit to desired position
        }
        return e;
    }

    private int shiftLeftPart(int v, int bits) {
        return ((v >> bits) ^ 0xFF) & 0xFF;
    }

    private int shiftRightPart(int v, int bits) {
        return ((v << (8-bits)) ^ 0xFF) & 0xFF;
    }
}
