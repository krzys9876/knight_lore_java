package org.kr.kl.objects;

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

    public void replaceAndRevertBytes(int pos1, int pos2) {
        int buffer = getByteRaw(pos1);
        setByteRaw(pos1, lookupTable.getReverse(getByteRaw(pos2)));
        setByteRaw(pos2, lookupTable.getReverse(buffer));
    }

    public void reverseByte(int pos) { setByteRaw(pos, lookupTable.getReverse(getByteRaw(pos)));}

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
                int firstByteAddress = de + line * bytesInLine;
                int lastByteAddress = firstByteAddress + bytesInLine - 1;
                int bytePairs = bytesInLine >> 1; // byte pair is a mask byte followed by data byte
                if((bytePairs & 1) == 1) {
                    // Middle pair means that we have off number of data bytes (don't first the mast) so we must only flip bits and leave bytes in place
                    // m1 b1 m2 b2 m3 b3 -> m2 b2 are the middle pair
                    bytePairs --;
                    int byteAddress = firstByteAddress + bytePairs;
                    reverseByte(byteAddress);
                    reverseByte(byteAddress + 1);
                }
                for(int b = 0; b < bytePairs / 2; b++) {
                    // NOTE: the data bytes and mask bytes must not be mixed
                    // m1 b1 m2 b2 m3 b3 m4 b4 -> m4 b4 m3 b3 m2 b2 m1 b1
                    int leftByteAddress = firstByteAddress + b * 2;
                    int rightByteAddress = lastByteAddress - b * 2 - 1;
                    replaceAndRevertBytes(leftByteAddress, rightByteAddress); // mask
                    replaceAndRevertBytes(leftByteAddress + 1, rightByteAddress + 1); // data
                }
            }
            toggleHFlip();
        }
    }

    public int applyToByte(int bufByte, int pos) {
        int outBufByte = bufByte;
        int e_mask = getMask(pos);
        int d_spriteByte = getByte(pos);
        outBufByte = (outBufByte & (e_mask ^ 0xFF));
        outBufByte = (outBufByte | d_spriteByte) & 0xFF;
        return outBufByte;
    }

    public int applyToByteLeft(int bufByte, int pos, int offset) { return applyToByte(bufByte, pos, offset, true); }
    public int applyToByteRight(int bufByte, int pos, int offset) { return applyToByte(bufByte, pos, offset, false); }

    public int applyToByte(int bufByte, int pos, int offset, boolean left) {
        int outBufByte = bufByte;
        int e_mask = (left ? lookupTable.getLeft(getMask(pos), offset) : lookupTable.getRight(getMask(pos), offset)) ^ 0xFF;
        int d_spriteByte = (left ? lookupTable.getLeft(getByte(pos), offset) : lookupTable.getRight(getByte(pos), offset)) ^ 0xFF;
        outBufByte = (outBufByte & (e_mask ^ 0xFF));
        outBufByte = (outBufByte | d_spriteByte) & 0xFF;
        return  outBufByte;
    }
}
