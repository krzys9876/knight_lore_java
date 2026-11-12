package org.kr.kl.objects;

import org.kr.DataBlock;

public class LookupTable {
    private final DataBlock data = new DataBlock(0xF100, 0xFFFF - 0xF100 + 1);

    public LookupTable() {
        build();
    }

    public int get(int address) { return data.getU(address); }

    private void build() {
        // NOTE: registers are treated as ints, not bytes due to lack of unsigned byte type in java

        // E is not reinitialized between the two loops
        int e=0;
        // F200 - FFFF
        for(int l = 0; l<=0xFF; l++) {
            int d=0;
            e=l;
            int h = 0xFF;
            for (int b = 7; b > 0; b--) {
                // SLA E
                boolean cy = (e & 0x80) > 0;
                e = (e << 1) & 0xFF;
                // RL D
                d = (d << 1) & 0xFF;
                if(cy) d++;
                // LD A, E
                int a = e;
                // CPL
                a = a ^ 0xFF;
                // LD (HL), A
                data.set(h * 256 + l, a);
                // DEC H
                h--;
                // LD A, D
                a = d;
                // CPL
                a = a ^ 0xFF;
                data.set(h * 256 + l, a);
                // DEC H
                h--;
                // DJNZ $D6A7
            }
        }
        // F100 - F1FF
        for(int l = 0; l<=0xFF; l++) data.set(0xF100+l, reverseBits(l));
        // Lookup table values verified with KL memory dump
    }

    private int reverseBits(int v) {
        int e = 0;
        for(int b=0; b<8; b++) {
            int bit = (v & (1 << b)) >> b; // given bit shifted to position 0
            e |= bit << (7-b); // shift bit to desired position
        }
        return e;
    }
}
