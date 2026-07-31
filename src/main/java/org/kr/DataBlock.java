package org.kr;

import java.util.Arrays;

public class DataBlock {
    public final int start;
    public final int size;
    private final int[] data;

    public DataBlock(int start, int size) {
        this.start = start;
        this.size = size;
        data = new int[size];
    }

    public DataBlock(int start, int[] data) {
        this.start = start;
        this.size = data.length;
        this.data = Arrays.copyOf(data, data.length);
    }

    public DataBlock copy() {
        return new DataBlock(start, Arrays.copyOf(data, data.length));
    }

    public int last() { return start + size - 1; }
    public int endExcl() { return start + size; }
    public void set(int address, int value) {
        if(value < -256 || value > 255)
            throw new IllegalArgumentException("value must be between 0 and 255");
        data[address - start]=value;
    }
    public int get(int address) { return data[address - start]; }
    public int[] getCopy() { return Arrays.copyOf(data, data.length); }

    public void reset() {
        for (int i = start; i < start+size-1; i++) { set(i, 0);}
    }
}
