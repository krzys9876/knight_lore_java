package org.kr;

import java.util.Arrays;

public abstract class VideoMemory {
    public static final int WIDTH = 256;
    public static final int HEIGHT = 192;
    public static final int PIXEL_MEM_SIZE = WIDTH / 8 * HEIGHT;

    // pixel memory: 32 bytes * 192 lines
    // attribute memory: 32 x 24 attribute "cells" (8x8 pixels each)
    // NOTE: using int instead of byte because there is no unsigned byte type in java
    protected final int[] memory = new int[PIXEL_MEM_SIZE + WIDTH / 8 * HEIGHT / 8];
    public final int start;

    public VideoMemory(int start) { this.start = start ; }

    public int[] getCopy() { return Arrays.copyOf(memory, memory.length); }
    public void setByteAt(int address, int value) { memory[address - start] = value ; }
    public int getByteAt(int address) { return memory[address - start]; }
}
