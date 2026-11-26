package org.kr.kl.objects;

import org.kr.kl.data.DataBlock;

public class Font {
    private final Char[] chars;
    private final String charsAsString;

    public Font(DataBlock data, String charsAsString) {
        int size = data.size / 8;
        chars = new Char[size];
        for(int i=0; i<size; i++) {
            chars[i] = new Char(getCharRaw(i, data));
        }
        this.charsAsString = charsAsString;
    }

    public int[] transcribe(String text) {
        int[] a = new int[text.length()];
        for(int i=0; i<text.length(); i++) {
            a[i] =  charsAsString.indexOf(text.charAt(i));
        }
        return a;
    }

    public int[] getChar(int ch) { return chars[ch & 0x7F].get(); }

    private static int[] getCharRaw(int ch, DataBlock data) {
        int[] a = new int[8];
        int baseAddr = data.start + (ch & 0x7F) * 8;
        for(int i=0; i<8; i++) a[i] = data.getU(baseAddr + i);
        return a;
    }
}

class Char {
    private final int[] chData;
    int[] get() { return chData; }

    Char(int[] chData) { this.chData = chData; }
}
