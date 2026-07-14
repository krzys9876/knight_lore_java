package org.kr;

/**
 * Linear screen layout
 */

public class VideoMemoryLinear extends VideoMemory {
    public VideoMemoryLinear(int start) {
        super(start);
    }

    @Override
    protected int getAttributeFromAddress(int address) {
        return memory[PIXEL_MEM_SIZE + (address & 0b11111) + ((address & 0b1111100000000) >> 3)];
    }

    @Override
    protected int getYFromAddress(int address) {
        return 191 - (address >> 5); // video buffer has reversed Y axis
    }
}
