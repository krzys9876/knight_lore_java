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
        for(int l = 0; l<=0xFF; l++) {
            int d=l;
            for(int b=8; b > 0; b--) {
                // SRL D
                boolean cy = (d & 1) > 0;
                d = (d >> 1);
                // RL E
                e = (e << 1) & 0xFF;
                if(cy) e++;
                // DJNZ $D6BE
            }
            // LD (HL), e
            data.set(0xF100+l, e);
        }
        // Lookup table values verified with KL memory dump
    }
}
