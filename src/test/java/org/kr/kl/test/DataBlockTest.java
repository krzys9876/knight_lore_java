package org.kr.kl.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kr.DataBlock;

public class DataBlockTest {
    @Test
    @DisplayName("Verify unsigned boolean values")
    public void shouldStoreAndReadUnsignedValue() {
        DataBlock block = new DataBlock(0, 256);
        for(int i = 0; i<256; i++) {
            block.set(i, i);
            int unsigned = block.getU(i);
            assert(unsigned == i);
        }
    }

    @Test
    @DisplayName("Verify signed boolean values")
    public void shouldStoreAndReadSignedValue() {
        DataBlock block = new DataBlock(0, 256);
        for(int i = -128; i<128; i++) {
            int addr = i + 128;
            block.set(addr, i);
            int signed = block.getS(addr);
            assert(signed == i);
        }
    }

    @Test
    @DisplayName("Verify conversion between signed and unsigned values")
    public void shouldConvertBetweenSignedAndUnsigned() {
        DataBlock block = new DataBlock(0, 256);
        for(int i = 0; i<255; i++) {
            int signedValue = i - 128;
            block.set(i, signedValue);
            int unsignedResult = block.getU(i);
            block.set(i, unsignedResult);
            int signedResult = block.getS(i);
            assert(signedResult == signedValue);
        }
    }
}
