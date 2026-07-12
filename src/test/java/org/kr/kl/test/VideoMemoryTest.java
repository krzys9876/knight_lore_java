package org.kr.kl.test;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kr.Color;
import org.kr.ScreenPanel;
import org.kr.VideoMemory;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VideoMemoryTest {
    @Test
    @DisplayName("Set the first bytes of pixel and attribute memory areas and test the outcome for the first 8 pixels")
    public void shouldPixelsReflectVideoMemoryLayoutWhenTopByteIsSet() {
        // given
        VideoMemory mem = new VideoMemory();
        // when
        // attribute - blue ink (001), red paper (010), brightness on, flash off
        mem.setByteAt(0x1800, (byte)0b01010001);
        mem.setByteAt(0,(byte)0b10101010);
        int[] pixels = mem.toPixels();
        // then
        // testing first 8 bytes
        for(int i=0; i<4; i++) {
            assertEquals(Color.getScreenColor(Color.BLUE, true), pixels[i*2]);
            assertEquals(Color.getScreenColor(Color.RED, true), pixels[i*2+1]);
        }
        saveToFile(mem, Path.of("test-files","test001.png").toString(), false);
    }

    @Test
    @DisplayName("Set a byte somewhere in the middle of the screen (pixel and attribute memory areas) and test the outcome")
    public void shouldPixelsReflectVideoMemoryLayoutWhenMiddleByteIsSet() {
        // given
        VideoMemory mem = new VideoMemory();
        // when
        // we should set each of the three areas (8x8 pixels high each) of the screen to ensure proper logic
        // we set attribute "cells": (6,0), (8,7), (10,8), (13,15), (17,16), (31,23)
        mem.setByteAt(0x1800 + 6, Color.getAttribute(Color.BLUE, Color.RED, Color.BRIGHT, Color.NONE));
        mem.setByteAt(0x1800 + 8 + 7*32, Color.getAttribute(Color.MAGENTA, Color.GREEN, Color.BRIGHT, Color.NONE));
        mem.setByteAt(0x1800 + 10 + 8*32, Color.getAttribute(Color.CYAN, Color.YELLOW, Color.BRIGHT, Color.FLASH));
        mem.setByteAt(0x1800 + 13 + 15*32, Color.getAttribute(Color.WHITE, Color.BLACK, Color.BRIGHT, Color.FLASH));
        mem.setByteAt(0x1800 + 17 + 16*32, Color.getAttribute(Color.BLUE, Color.GREEN, Color.NONE, Color.NONE));
        mem.setByteAt(0x1800 + 31 + 23*32, Color.getAttribute(Color.WHITE, Color.RED, Color.NONE, Color.NONE));

        fillOneCell(mem, 6,0);
        fillOneCell(mem, 8,7);
        fillOneCell(mem, 10,8);
        fillOneCell(mem, 13,15);
        fillOneCell(mem, 17,16);
        fillOneCell(mem, 31,23);

        // then
        // screen reflects what we put into memory
        verifyOneCell(mem, 6, 0, Color.BLUE, Color.RED, true, false);
        verifyOneCell(mem, 8, 7, Color.MAGENTA, Color.GREEN, true, false);
        verifyOneCell(mem, 10, 8, Color.CYAN, Color.YELLOW, true, true);
        verifyOneCell(mem, 13, 15, Color.WHITE, Color.BLACK, true, true);
        verifyOneCell(mem, 17, 16, Color.BLUE, Color.GREEN, false, false);
        verifyOneCell(mem, 31, 23, Color.WHITE, Color.RED, false, false);

        saveToFile(mem, Path.of("test-files","test002_noflash.png").toString(), false);
        saveToFile(mem, Path.of("test-files","test002_flash.png").toString(), true);
    }

    private void verifyOneCell(VideoMemory mem, int x, int y, int pen, int paper, boolean bright, boolean flash) {
        int[] pixels = mem.toPixels(flash);
        for(int iy=0; iy<8; iy++) {
            for(int ix=0; ix<8; ix++) {
                boolean isPen = (((ix+iy) & 1) == 0) ^ flash ;
                assertEquals(Color.getScreenColor(isPen ? paper : pen, bright),
                        pixels[x * 8 + ix + (y*8 + iy) * VideoMemory.WIDTH]);
            }
        }
    }

    private void fillOneCell(VideoMemory mem, int x, int y) {
        // Note: the screen is divided into 3 areas, each is 8 cells high
        for(int i=0; i<8; i++) {
            mem.setByteAt(x + 32*8*8*(y/8) + 32*(y & 7) + 32*8 * i, (i & 1)==0 ? (byte)0b01010101 : (byte)0b10101010);
        }
    }

    private void saveToFile(VideoMemory mem, String path, boolean flash) {
        ScreenPanel panel = new ScreenPanel();
        panel.setPixelData(mem.toPixels(flash));
        try {
            panel.saveImage(path);
        } catch(IOException e) {
            e.printStackTrace();
        }

    }
}
