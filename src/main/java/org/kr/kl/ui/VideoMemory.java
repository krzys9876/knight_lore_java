package org.kr.kl.ui;

import org.kr.kl.data.DataBlock;

import java.util.Arrays;

public abstract class VideoMemory {
    public static final int WIDTH = 256;
    public static final int HEIGHT = 192;
    public static final int PIXEL_MEM_SIZE = WIDTH / 8 * HEIGHT;
    public static final int ATTR_MEM_SIZE = WIDTH / 8 * HEIGHT / 8;

    // pixel memory: 32 bytes * 192 lines
    // attribute memory: 32 x 24 attribute "cells" (8x8 pixels each)
    // NOTE: using int instead of byte because there is no unsigned byte type in java
    protected final int[] memory = new int[PIXEL_MEM_SIZE + WIDTH / 8 * HEIGHT / 8];
    public final int attrStart;
    public final int endExcl;

    public VideoMemory() {
        this.attrStart = PIXEL_MEM_SIZE;
        this.endExcl = this.attrStart + ATTR_MEM_SIZE;
    }

    public int[] getCopy() { return Arrays.copyOf(memory, memory.length); }
    public void setByteAt(int address, int value) { memory[address] = value ; }
    public void clear() { Arrays.fill(memory, 0); }
    public void clearAndSetBackground() {
        clear();
        setVisibleBackground(Color.getAttribute(Color.WHITE, Color.BLUE, true, false));
    }
    public void setVisibleBackground(int attr) {
        for(int hl = PIXEL_MEM_SIZE; hl < PIXEL_MEM_SIZE + 0x0300; hl++) setByteAt(hl, attr);
    }
    public int getByteAt(int address) { return memory[address]; }

    public int[] toPixels(long tick) {
        return toPixels(tick == -1 || ((tick / 5) % 2) == 0);
    }

    public int[] toPixels(boolean flash) {
        int[] pixels = new int[WIDTH * HEIGHT];
        for(int i = 0; i < PIXEL_MEM_SIZE; i++) {
            int attribute = getAttributeFromAddress(i);
            boolean attrFlash = (attribute & 0x80) > 0;
            int origPen = attribute & 0b111;
            int origPaper = (attribute & 0b111000) >> 3;
            int pen = (attrFlash && flash) ? origPaper : origPen;
            int paper = (attrFlash && flash) ? origPen : origPaper;
            boolean bright = (attribute & 0b1000000) > 0;
            int pixelData = memory[i];
            int baseX = getXFromAddress(i);
            int baseY = getYFromAddress(i);
            //IO.println("i="+i+", baseX="+baseX+", baseY="+baseY);
            for(int b = 0; b<8; b++) {
                boolean isSet = (pixelData & (1 << (7-b))) > 0;
                pixels[baseX + baseY * WIDTH + b] = Color.getScreenColor(isSet ? pen : paper, bright);
            }
        }
        return pixels;
    }

    abstract protected int getAttributeFromAddress(int address);

    protected int getXFromAddress(int address) {
        return (address & 0b11111) << 3;
    }

    abstract protected int getYFromAddress(int address);

    public void printCharacter(int character, int address, DataBlock font) {
        int baseChar = font.start;
        int charPointer = (character & 0x7F) * 8 + baseChar; // font address
        for(int i = 0; i<8; i++) setByteAt(address - i*0x20, font.getU(charPointer + i));
    }

    public static int calcVideoAddr(int y, int x) { return (x >> 3) + y * 0x20; }

    public static int calcAttribAddr(int h, int l) {
        // h: y, l: x
        int y = (h ^ 0xFF) >> 3;
        int xy = ((y - 8) * 0x20) + (l >> 3);
        return PIXEL_MEM_SIZE + xy;
    }

}
