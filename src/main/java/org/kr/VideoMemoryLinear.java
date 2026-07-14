package org.kr;

public class VideoMemoryLinear extends VideoMemory {
    public VideoMemoryLinear(int start) {
        super(start);
    }

    public int[] toPixels(boolean flash) {
        int[] pixels = new int[WIDTH * HEIGHT];
        for(int i = 0; i < PIXEL_MEM_SIZE; i++) {
            int attribute = memory[PIXEL_MEM_SIZE + (i & 0b11111) + ((i & 0b1111100000000) >> 3)];
            boolean attrFlash = (attribute & 0x80) > 0;
            int origPen = attribute & 0b111;
            int origPaper = (attribute & 0b111000) >> 3;
            int pen = (attrFlash && flash) ? origPaper : origPen;
            int paper = (attrFlash && flash) ? origPen : origPaper;
            boolean bright = (attribute & 0b1000000) > 0;
            int pixelData = memory[i];
            int baseX = (i & 0b11111) << 3;
            int baseY = i >> 5;
            //IO.println("i="+i+", baseX="+baseX+", baseY="+baseY);
            for(int b = 0; b<8; b++) {
                boolean isSet = (pixelData & (1 << (7-b))) > 0;
                pixels[baseX + baseY * WIDTH + b] = Color.getScreenColor(isSet ? pen : paper, bright);
            }
        }
        return pixels;
    }
}
