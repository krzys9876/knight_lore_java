package org.kr.kl.ui;

import org.kr.kl.objects.SpriteData;

/**
 * Follows ZX Spectrum screen layout
 */
public class VideoMemoryScreen extends VideoMemory {
    public VideoMemoryScreen(SpriteData spriteData) {
        super(spriteData);
    }

    @Override
    protected int getAttributeFromAddress(int address) {
        return memory[PIXEL_MEM_SIZE + (address & 0b11111111) + ((address & 0b1100000000000) >> 3)];
    }

    @Override
    protected int getYFromAddress(int address) {
        return ((address & 0b11100000000) >> 8) + ((address & 0b11100000) >> 2) + ((address & 0b1100000000000) >> 5);
    }
}

