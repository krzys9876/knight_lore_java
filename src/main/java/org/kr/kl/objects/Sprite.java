package org.kr.kl.objects;

public class Sprite {
    public int[] data;

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

    public void setMask(int pos, int m) { data[2 + pos*2] = m; }
    public void setByte(int pos, int b) { data[2 + pos*2+1] = b; }
    public void setByteRaw(int pos, int b) { data[2 + pos] = b; }

    public void replaceBytes(int pos1, int pos2) {
        int buffer = getByteRaw(pos1);
        setByteRaw(pos1, getByteRaw(pos2));
        setByteRaw(pos2, buffer);

    }
}
