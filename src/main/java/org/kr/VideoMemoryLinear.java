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
        int x = address & 0x1F;
        int y = (address & 0b11111111100000) >> 8;
        y = 23 - y;
        int attrAddress = (y << 5) + x;
        return memory[PIXEL_MEM_SIZE + attrAddress];
    }

    @Override
    protected int getYFromAddress(int address) {
        return 191 - (address >> 5); // video buffer has reversed Y axis
    }
}
