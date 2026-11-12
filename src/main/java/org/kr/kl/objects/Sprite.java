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
}
