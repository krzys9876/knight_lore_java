package org.kr;

import java.time.LocalDateTime;
import java.util.Arrays;

public class VideoMemory {
    public static final int WIDTH = 256;
    public static final int HEIGHT = 192;
    public static final int PIXEL_MEM_SIZE = WIDTH/8 * HEIGHT;

    // pixel memory: 32 bytes * 192 lines
    // attribute memory: 32 x 24 attribute "cells" (8x8 pixels each)
    // NOTE: using int instead of byte because there is no unsigned byte type in java
    private final int[] memory = new int[PIXEL_MEM_SIZE + WIDTH/8 * HEIGHT/8];
    public final int start;

    public VideoMemory(int start) {
        this.start = start;
    }

    public int[] getCopy() { return Arrays.copyOf(memory, memory.length); }

    public void setByteAt(int address, int value) { memory[address - start] = value;}

    public int getByteAt(int address) { return memory[address - start];}

    public int[] toPixels(LocalDateTime time) {
        return toPixels((time.getSecond() % 2) == 0);
    }

    public int[] toPixels() { return toPixels(false); }

    //public int[]

    public int[] toPixels(boolean flash) {
        int[] pixels = new int[WIDTH * HEIGHT];
        for(int i = 0; i < PIXEL_MEM_SIZE; i++) {
            int attribute = memory[PIXEL_MEM_SIZE + (i & 0b11111111) + ((i & 0b1100000000000) >> 3)];
            boolean attrFlash = (attribute & 0x80) > 0;
            int origPen = attribute & 0b111;
            int origPaper = (attribute & 0b111000) >> 3;
            int pen = (attrFlash && flash) ? origPaper : origPen;
            int paper = (attrFlash && flash) ? origPen : origPaper;
            boolean bright = (attribute & 0b1000000) > 0;
            int pixelData = memory[i];
            int baseX = (i & 0b11111) * 8;
            int baseY = ((i & 0b11100000000) >> 8) + ((i & 0b11100000) >> 2) + ((i & 0b1100000000000) >> 5);
            //IO.println("i="+i+", baseX="+baseX+", baseY="+baseY);
            for(int b = 0; b<8; b++) {
                boolean isSet = (pixelData & (1 << (7-b))) > 0;
                pixels[baseX + baseY * WIDTH + b] = Color.getScreenColor(isSet ? pen : paper, bright);
            }
        }
        return pixels;
    }
}

