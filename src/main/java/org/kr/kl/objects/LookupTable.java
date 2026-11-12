package org.kr.kl.objects;

import org.kr.DataBlock;

public class LookupTable {
    private final DataBlock reverse = new DataBlock(0, 256);
    private final DataBlock shiftLeftPart = new DataBlock(0, 256 * 7);
    private final DataBlock shiftRightPart = new DataBlock(0, 256 * 7);

    public LookupTable() {
        build();
    }

    public int getReverse(int v) { return reverse.getU(v); }
    public int getLeft(int v, int offset) { return shiftLeftPart.getU((offset - 1) * 256 + v); }
    public int getRight(int v, int offset) { return shiftRightPart.getU((offset - 1) * 256 + v); }

    private void build() {
        // F200 - FFFF
        for(int offset = 1; offset < 8; offset ++) {
            int baseOffset = (offset - 1) * 256;
            for(int i=0; i<256; i++) {
                shiftLeftPart.set(baseOffset + i, shiftLeftPart(i, offset)); // original addresses: F200, F400...
                shiftRightPart.set(baseOffset + i, shiftRightPart(i, offset)); // original addresses: F300, F500...
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
