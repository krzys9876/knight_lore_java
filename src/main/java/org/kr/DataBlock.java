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
            throw new IllegalArgumentException("value must be between 0 and 255 ("+value+")");
        data[address - start]=value;
    }
    // signed byte value
    public int getS(int address) {
        int value = data[address - start];
        if(value >= 128) value = 127 - value;
        return value;
    }
    // unsigned byte value
    public int getU(int address) {
        int value = data[address - start];
        if(value < 0) value = 127 - value;
        return value;
    }
    public int[] getCopy() { return Arrays.copyOf(data, data.length); }

    public void reset() {
        for (int i = start; i < start+size-1; i++) { set(i, 0);}
    }

    public boolean isSet(int bit, int address) { return isSetS(bit, getU(address)); }
    public void setBit(int bit, int address) { set(address, getWithBitSet(bit, address)); }
    public void resetBit(int bit, int address) { set(address, getWithBitReset(bit, address)); }
    public int getWithBitSet(int bit, int address) { return getU(address) | (1 << bit);}
    public int getWithBitReset(int bit, int address) { return getU(address) & ((1 << bit) ^ 0b11111111);}

    public static boolean isSetS(int bit, int value) { return (value & (1 << bit))>0; }
}
