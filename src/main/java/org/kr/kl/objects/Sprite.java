package org.kr.kl.objects;

import java.util.Optional;

public class Sprite {
    private final int[] data;
    private static final LookupTable lookupTable = new LookupTable();

    public final int lines;
    public final int width;
    public final int bytesInLine;

    public Sprite(int[] data) {
        this.data = data;
        lines = data[1];
        width = data[0] & 7;
        bytesInLine = width * 2;
    }

    public int getMask(int pos) { return data[2 + pos*2]; }
    public int getByte(int pos) { return data[2 + pos*2+1]; }
    public int getByteRaw(int pos) { return data[2 + pos]; }

    public void setByteRaw(int pos, int b) { data[2 + pos] = b; }

    public void replaceBytes(int pos1, int pos2) {
        int buffer = getByteRaw(pos1);
        setByteRaw(pos1, getByteRaw(pos2));
        setByteRaw(pos2, buffer);
    }

    public void toggleHFlip() { data[0] = data[0] ^ 0x40; }
    public void toggleVFlip() { data[0] = data[0] ^ 0x80; }

    public boolean isHFlip() { return (data[0] & 0x40) > 0; }
    public boolean isVFlip() { return (data[0] & 0x80) > 0; }

    public void flip(boolean refHFlip, boolean refVFlip) {
        int de = 0;

        // Flip vertically (in-place) and set flag in data
        if(refVFlip != isVFlip()) {
            for(int line = 0; line<(lines >> 1); line++) {
                for(int b = 0; b < bytesInLine; b++) {
                    int topByteAddress = de + line * bytesInLine + b;
                    int bottomByteAddress = de + (lines - line - 1) * bytesInLine + b;
                    replaceBytes(topByteAddress, bottomByteAddress);
                }
            }
            toggleVFlip();
        }
        // Flip horizontally (in-place) and set flag in data
        if(refHFlip != isHFlip()) {
            for(int line = 0; line<lines; line++) {
                int firstByteAddress = de + 2 + line * bytesInLine;
                int lastByteAddress = firstByteAddress + bytesInLine - 1;
                int bytePairs = bytesInLine >> 1;
                for(int b = 0; b < bytePairs; b++) {
                    boolean isMask = (b & 1) == 0;
                    // Middle pair means that we have off number of data bytes so we must only flip bits and leave bytes in place
                    boolean isMiddlePair = ((bytePairs & 1) == 1) && (b == bytePairs - 1);
                    int shift = isMiddlePair ? 0 : (isMask ? -1 : 1);
                    // NOTE: the data bytes and mask bytes must not be mixed
                    int leftByteAddress = firstByteAddress + b;
                    int rightByteAddress = lastByteAddress - b + shift;
                    int leftBuffer = getByteRaw(leftByteAddress - 2);
                    leftBuffer = lookupTable.get(0xF100 + leftBuffer); // F1xx - byte flip table
                    int rightBuffer = getByteRaw(rightByteAddress - 2);
                    rightBuffer = lookupTable.get(0xF100 + rightBuffer);
                    setByteRaw(leftByteAddress - 2, isMiddlePair ? leftBuffer : rightBuffer);
                    setByteRaw(rightByteAddress - 2, isMiddlePair ? rightBuffer : leftBuffer);
                }
            }
            toggleHFlip();
        }
    }
}
