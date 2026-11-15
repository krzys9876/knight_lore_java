package org.kr.kl.objects;

public record LocationForegroundObject(int grNo, int cnt, int[] coords) {
    public int x(int i) { return (coords[i] & 0b00000111); }
    public int y(int i) { return (coords[i] & 0b00111000) >> 3; }
    public int z(int i) { return (coords[i] & 0b11000000) >> 6; }
}
